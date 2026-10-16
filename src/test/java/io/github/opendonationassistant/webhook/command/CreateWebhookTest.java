package io.github.opendonationassistant.webhook.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.webhook.command.CreateWebhook.CreateWebhookCommand;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.security.authentication.Authentication;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pure Mockito unit tests for {@link CreateWebhook}. The owner is always derived from the JWT
 * attributes, never from the request body.
 */
public class CreateWebhookTest {

  WebhookRepository repository = mock(WebhookRepository.class);
  CreateWebhook controller = new CreateWebhook(repository);
  Authentication auth = mock(Authentication.class);

  @Test
  public void testCreatesWebhookForAuthenticatedOwner() {
    // Arrange
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipientId")
    );
    var created = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      true
    );
    when(
      repository.create(
        "recipientId",
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        true
      )
    ).thenReturn(created);

    // Act
    var response = controller
      .createWebhook(
        auth,
        new CreateWebhookCommand(
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          true
        )
      )
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals("webhookId", body.id());
    assertEquals("recipientId", body.recipientId());
    assertEquals("https://example.com/hook", body.url());
    assertEquals(Webhook.Type.DISCORD, body.type());
    assertTrue(body.enabled());
    verify(repository).create(
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      true
    );
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    when(auth.getAttributes()).thenReturn(Map.of());

    // Act
    var response = controller
      .createWebhook(
        auth,
        new CreateWebhookCommand(
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          true
        )
      )
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).create(
      anyString(),
      anyString(),
      any(),
      anyBoolean()
    );
  }
}
