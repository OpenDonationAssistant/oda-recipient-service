package io.github.opendonationassistant.webhook.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.webhook.command.UpdateWebhook.UpdateWebhookCommand;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.security.authentication.Authentication;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Pure Mockito unit tests for {@link UpdateWebhook}. Not-found and non-owner both collapse to 404 so
 * webhook existence is never leaked.
 */
public class UpdateWebhookTest {

  WebhookRepository repository = mock(WebhookRepository.class);
  UpdateWebhook controller = new UpdateWebhook(repository);
  Authentication auth = mock(Authentication.class);

  @Test
  public void testUpdatesWebhookOwnedByAuthenticatedRecipient() {
    // Arrange
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipientId")
    );
    when(repository.findById("webhookId")).thenReturn(
      Optional.of(
        new Webhook(
          "webhookId",
          "recipientId",
          "https://example.com/old",
          Webhook.Type.DISCORD,
          true
        )
      )
    );
    when(
      repository.update(
        "webhookId",
        "recipientId",
        "https://example.com/updated",
        Webhook.Type.TELEGRAM,
        false
      )
    ).thenReturn(
      new Webhook(
        "webhookId",
        "recipientId",
        "https://example.com/updated",
        Webhook.Type.TELEGRAM,
        false
      )
    );

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(
          "webhookId",
          "https://example.com/updated",
          Webhook.Type.TELEGRAM,
          false
        )
      )
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals("webhookId", body.id());
    assertEquals("recipientId", body.recipientId());
    assertEquals("https://example.com/updated", body.url());
    assertEquals(Webhook.Type.TELEGRAM, body.type());
    assertFalse(body.enabled());
    verify(repository).update(
      "webhookId",
      "recipientId",
      "https://example.com/updated",
      Webhook.Type.TELEGRAM,
      false
    );
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    when(auth.getAttributes()).thenReturn(Map.of());

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(
          "webhookId",
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          true
        )
      )
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).findById(anyString());
    verify(repository, never()).update(
      anyString(),
      anyString(),
      anyString(),
      any(),
      anyBoolean()
    );
  }

  @Test
  public void testReturnsNotFoundWhenWebhookDoesNotExist() {
    // Arrange
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipientId")
    );
    when(repository.findById("webhookId")).thenReturn(Optional.empty());

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(
          "webhookId",
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          true
        )
      )
      .join();

    // Assert
    assertEquals(404, response.getStatus().getCode());
    verify(repository, never()).update(
      anyString(),
      anyString(),
      anyString(),
      any(),
      anyBoolean()
    );
  }

  @Test
  public void testReturnsNotFoundWhenWebhookOwnedByAnotherRecipient() {
    // Arrange
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipientId")
    );
    when(repository.findById("webhookId")).thenReturn(
      Optional.of(
        new Webhook(
          "webhookId",
          "otherRecipientId",
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          true
        )
      )
    );

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(
          "webhookId",
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          true
        )
      )
      .join();

    // Assert
    assertEquals(404, response.getStatus().getCode());
    verify(repository, never()).update(
      anyString(),
      anyString(),
      anyString(),
      any(),
      anyBoolean()
    );
  }
}
