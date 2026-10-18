package io.github.opendonationassistant.webhook.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent.Event;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Pure Mockito unit tests for {@link WebhookRepository}. Verifies persistence side effects, the
 * owner-scoped soft-delete/update guards, and the RabbitMQ events emitted on create/update/delete.
 */
public class WebhookRepositoryTest {

  private static final Map<String, Object> SETTINGS = Map.of(
    "channel",
    "general"
  );

  WebhookDataRepository dataRepository = mock(WebhookDataRepository.class);
  RabbitClient events = mock(RabbitClient.class);

  private WebhookRepository repositoryWithMocks() {
    return new WebhookRepository(dataRepository, events);
  }

  @Test
  public void testCreateSavesWebhookAndEmitsCreatedEvent() throws IOException {
    // Arrange
    var repository = repositoryWithMocks();

    // Act
    var created = repository.create(
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true
    );

    // Assert - persistence
    var savedWebhook = ArgumentCaptor.forClass(Webhook.class);
    verify(dataRepository).save(savedWebhook.capture());
    assertEquals("recipientId", savedWebhook.getValue().recipientId());
    assertEquals("https://example.com/hook", savedWebhook.getValue().url());
    assertEquals(Webhook.Type.DISCORD, savedWebhook.getValue().type());
    assertEquals(SETTINGS, savedWebhook.getValue().settings());
    assertTrue(savedWebhook.getValue().enabled());
    assertFalse(savedWebhook.getValue().deleted(), "new webhook must be active");
    assertFalse(
      savedWebhook.getValue().id().isEmpty(),
      "generated id must not be empty"
    );

    // Assert - event
    var eventCaptor = ArgumentCaptor.forClass(WebhookChangedEvent.class);
    verify(events).sendEvent(eventCaptor.capture());
    var event = eventCaptor.getValue();
    assertEquals(Event.WEBHOOK_CREATED, event.event());
    assertEquals("recipientId", event.recipientId());
    assertEquals("https://example.com/hook", event.url());
    assertEquals(Webhook.Type.DISCORD, event.type());
    assertEquals(SETTINGS, event.settings());
    assertTrue(event.enabled());
    assertEquals(created.id(), event.id());
  }

  @Test
  public void testUpdateUpdatesWebhookAndEmitsUpdatedEvent()
    throws IOException {
    // Arrange
    var repository = repositoryWithMocks();
    var webhook = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/updated",
      Webhook.Type.TELEGRAM,
      Map.of("chatId", "12345"),
      false,
      false
    );
    when(
      dataRepository.existsByIdAndRecipientIdAndDeletedFalse(
        "webhookId",
        "recipientId"
      )
    ).thenReturn(true);

    // Act
    var result = repository.update(webhook);

    // Assert - persistence
    assertTrue(result.isPresent());
    var updatedWebhook = ArgumentCaptor.forClass(Webhook.class);
    verify(dataRepository).update(updatedWebhook.capture());
    assertEquals(webhook, updatedWebhook.getValue());
    verify(dataRepository, never()).save(any(Webhook.class));

    // Assert - event
    var eventCaptor = ArgumentCaptor.forClass(WebhookChangedEvent.class);
    verify(events).sendEvent(eventCaptor.capture());
    var event = eventCaptor.getValue();
    assertEquals(Event.WEBHOOK_UPDATED, event.event());
    assertEquals("webhookId", event.id());
    assertEquals(Webhook.Type.TELEGRAM, event.type());
    assertEquals(Map.of("chatId", "12345"), event.settings());
    assertFalse(event.enabled());
  }

  @Test
  public void testUpdateReturnsEmptyAndEmitsNoEventWhenNotOwnedOrDeleted()
    throws IOException {
    // Arrange
    var repository = repositoryWithMocks();
    var webhook = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true,
      false
    );
    when(
      dataRepository.existsByIdAndRecipientIdAndDeletedFalse(
        "webhookId",
        "recipientId"
      )
    ).thenReturn(false);

    // Act
    var result = repository.update(webhook);

    // Assert
    assertTrue(result.isEmpty());
    verify(dataRepository, never()).update(any(Webhook.class));
    verify(events, never()).sendEvent(any());
  }

  @Test
  public void testDeleteSoftDeletesAndEmitsDeletedEvent() throws IOException {
    // Arrange
    var repository = repositoryWithMocks();
    var webhook = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true,
      false
    );
    when(
      dataRepository.existsByIdAndRecipientIdAndDeletedFalse(
        "webhookId",
        "recipientId"
      )
    ).thenReturn(true);

    // Act
    var deleted = repository.delete(webhook);

    // Assert - soft delete, not a physical delete
    assertTrue(deleted);
    var updated = ArgumentCaptor.forClass(Webhook.class);
    verify(dataRepository).update(updated.capture());
    assertTrue(updated.getValue().deleted(), "row must be soft-deleted");
    verify(dataRepository, never()).deleteById(anyString());

    // Assert - event
    var eventCaptor = ArgumentCaptor.forClass(WebhookChangedEvent.class);
    verify(events).sendEvent(eventCaptor.capture());
    var event = eventCaptor.getValue();
    assertEquals(Event.WEBHOOK_DELETED, event.event());
    assertEquals("webhookId", event.id());
    assertEquals("recipientId", event.recipientId());
  }

  @Test
  public void testDeleteReturnsFalseAndEmitsNoEventWhenNotOwnedOrDeleted()
    throws IOException {
    // Arrange
    var repository = repositoryWithMocks();
    var webhook = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true,
      false
    );
    when(
      dataRepository.existsByIdAndRecipientIdAndDeletedFalse(
        "webhookId",
        "recipientId"
      )
    ).thenReturn(false);

    // Act
    var deleted = repository.delete(webhook);

    // Assert
    assertFalse(deleted);
    verify(dataRepository, never()).update(any(Webhook.class));
    verify(events, never()).sendEvent(any());
  }

  @Test
  public void testFindByIdReturnsEmptyForMalformedId() {
    // Arrange
    var repository = repositoryWithMocks();

    // Act - a non-UUID id must not reach the uuid column binder
    var result = repository.findById("not-a-uuid");

    // Assert
    assertTrue(result.isEmpty());
    verify(dataRepository, never()).findByIdAndDeletedFalse(anyString());
  }

  @Test
  public void testCreateWrapsIoExceptionWhenEventEmissionFails()
    throws IOException {
    // Arrange
    var repository = repositoryWithMocks();
    doThrow(new IOException("broker unavailable"))
      .when(events)
      .sendEvent(any());

    // Act + Assert
    assertThrows(RuntimeException.class, () ->
      repository.create(
        "recipientId",
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        SETTINGS,
        true
      )
    );
  }
}
