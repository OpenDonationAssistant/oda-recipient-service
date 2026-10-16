package io.github.opendonationassistant.webhook.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent.Event;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Pure Mockito unit tests for {@link WebhookRepository}. Verifies persistence side effects and the
 * RabbitMQ events emitted on create/update.
 */
public class WebhookRepositoryTest {

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
      true
    );

    // Assert - persistence
    var savedWebhook = ArgumentCaptor.forClass(Webhook.class);
    verify(dataRepository).save(savedWebhook.capture());
    assertEquals("recipientId", savedWebhook.getValue().recipientId());
    assertEquals("https://example.com/hook", savedWebhook.getValue().url());
    assertEquals(Webhook.Type.DISCORD, savedWebhook.getValue().type());
    assertTrue(savedWebhook.getValue().enabled());
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
    assertTrue(event.enabled());
    assertEquals(created.id(), event.id());
    assertFalse(event.id().isEmpty(), "generated id must not be empty");
  }

  @Test
  public void testUpdateUpdatesWebhookAndEmitsUpdatedEvent()
    throws IOException {
    // Arrange
    var repository = repositoryWithMocks();

    // Act
    var updated = repository.update(
      "webhookId",
      "recipientId",
      "https://example.com/updated",
      Webhook.Type.TELEGRAM,
      false
    );

    // Assert - persistence
    var updatedWebhook = ArgumentCaptor.forClass(Webhook.class);
    verify(dataRepository).update(updatedWebhook.capture());
    assertEquals("webhookId", updatedWebhook.getValue().id());
    assertEquals("recipientId", updatedWebhook.getValue().recipientId());
    assertEquals(
      "https://example.com/updated",
      updatedWebhook.getValue().url()
    );
    assertEquals(Webhook.Type.TELEGRAM, updatedWebhook.getValue().type());
    assertFalse(updatedWebhook.getValue().enabled());

    // Assert - event
    var eventCaptor = ArgumentCaptor.forClass(WebhookChangedEvent.class);
    verify(events).sendEvent(eventCaptor.capture());
    var event = eventCaptor.getValue();
    assertEquals(Event.WEBHOOK_UPDATED, event.event());
    assertEquals("webhookId", event.id());
    assertEquals("recipientId", event.recipientId());
    assertEquals("https://example.com/updated", event.url());
    assertEquals(Webhook.Type.TELEGRAM, event.type());
    assertFalse(event.enabled());
    assertEquals(updated.id(), event.id());

    // Update must not insert a new row.
    verify(dataRepository, never()).save(any(Webhook.class));
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
        true
      )
    );
  }
}
