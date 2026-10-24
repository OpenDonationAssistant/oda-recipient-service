package io.github.opendonationassistant.webhook.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.webhook.command.DeleteWebhook.DeleteWebhookCommand;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.ClientAuthentication;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Pure Mockito unit tests for {@link DeleteWebhook}. Only the JWT owner may delete; missing,
 * non-owner and a lost row all collapse to 401 so webhook existence is never leaked. Request-body
 * constraints are covered by {@link WebhookValidationTest}.
 */
public class DeleteWebhookTest {

  private static final String ID = "11111111-1111-1111-1111-111111111111";

  WebhookRepository repository = mock(WebhookRepository.class);
  DeleteWebhook controller = new DeleteWebhook(repository);
  Authentication auth = new ClientAuthentication(
    "user",
    Map.of("preferred_username", "recipientId")
  );

  @Test
  public void testDeletesWebhookOwnedByAuthenticatedRecipient() {
    // Arrange
    var webhook = new Webhook(
      ID,
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(webhook));
    when(repository.delete(webhook)).thenReturn(true);

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand(ID))
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    verify(repository).delete(webhook);
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    auth = new ClientAuthentication("user", Map.of());

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand(ID))
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).findById(anyString());
  }

  @Test
  public void testReturnsUnauthorizedWhenWebhookDoesNotExist() {
    // Arrange
    when(repository.findById(ID)).thenReturn(Optional.empty());

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand(ID))
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).delete(any(Webhook.class));
  }

  @Test
  public void testReturnsUnauthorizedWhenWebhookOwnedByAnotherRecipient() {
    // Arrange
    var webhook = new Webhook(
      ID,
      "otherRecipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(webhook));

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand(ID))
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).delete(any(Webhook.class));
  }

  @Test
  public void testReturnsUnauthorizedWhenDeleteReportsLostRow() {
    // Arrange
    var webhook = new Webhook(
      ID,
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(webhook));
    when(repository.delete(webhook)).thenReturn(false);

    // Act
    var response = controller
      .deleteWebhook(auth, new DeleteWebhookCommand(ID))
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
  }
}
