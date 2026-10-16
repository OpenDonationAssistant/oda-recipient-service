package io.github.opendonationassistant.webhook.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.webhook.command.DeleteWebhook.DeleteWebhookCommand;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.security.authentication.Authentication;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Pure Mockito unit tests for {@link DeleteWebhook}. Only the JWT owner may delete; not-found and
 * non-owner both collapse to 404 so webhook existence is never leaked.
 */
public class DeleteWebhookTest {

  WebhookRepository repository = mock(WebhookRepository.class);
  DeleteWebhook controller = new DeleteWebhook(repository);
  Authentication auth = mock(Authentication.class);

  @Test
  public void testDeletesWebhookOwnedByAuthenticatedRecipient() {
    // Arrange
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipientId")
    );
    var webhook = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      true
    );
    when(repository.findById("webhookId")).thenReturn(Optional.of(webhook));

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand("webhookId"))
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    verify(repository).delete(webhook);
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    when(auth.getAttributes()).thenReturn(Map.of());

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand("webhookId"))
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).findById(anyString());
    verify(repository, never()).delete(any(Webhook.class));
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
      .deleteWebhook(auth, new DeleteWebhookCommand("webhookId"))
      .join();

    // Assert
    assertEquals(404, response.getStatus().getCode());
    verify(repository, never()).delete(any(Webhook.class));
  }
  
}
