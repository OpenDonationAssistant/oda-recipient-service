package io.github.opendonationassistant.webhook.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.webhook.command.CreateWebhook.CreateWebhookCommand;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.ClientAuthentication;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pure Mockito unit tests for {@link CreateWebhook}. The owner is always derived from the JWT
 * attributes, never from the request body. Request-body constraints are covered by
 * {@link WebhookValidationTest}.
 */
public class CreateWebhookTest {

  private static final Map<String, Object> SETTINGS = Map.of(
    "channel",
    "general"
  );

  WebhookRepository repository = mock(WebhookRepository.class);
  CreateWebhook controller = new CreateWebhook(repository);
  Authentication auth = new ClientAuthentication(
    "user",
    Map.of("preferred_username", "recipientId")
  );

  @Test
  public void testCreatesWebhookForAuthenticatedOwner() {
    // Arrange
    var created = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true,
      false
    );
    when(
      repository.create(
        "recipientId",
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        SETTINGS,
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
          SETTINGS,
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
    assertEquals(SETTINGS, body.settings());
    assertTrue(body.enabled());
    verify(repository).create(
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true
    );
  }

  @Test
  public void testDefaultsSettingsToEmptyMapWhenOmitted() {
    // Arrange
    var created = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true,
      false
    );
    when(
      repository.create(
        "recipientId",
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        Map.of(),
        true
      )
    ).thenReturn(created);

    // Act - settings omitted (null)
    var response = controller
      .createWebhook(
        auth,
        new CreateWebhookCommand(
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          null,
          true
        )
      )
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    verify(repository).create(
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true
    );
  }

  @Test
  public void testDefaultsEnabledToTrueWhenOmitted() {
    // Arrange
    var created = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true,
      false
    );
    when(
      repository.create(
        "recipientId",
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        SETTINGS,
        true
      )
    ).thenReturn(created);

    // Act - enabled omitted (null)
    var response = controller
      .createWebhook(
        auth,
        new CreateWebhookCommand(
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          SETTINGS,
          null
        )
      )
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    verify(repository).create(
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      true
    );
  }

  @Test
  public void testHonoursExplicitlyDisabledWebhook() {
    // Arrange
    var created = new Webhook(
      "webhookId",
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      false,
      false
    );
    when(
      repository.create(
        "recipientId",
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        SETTINGS,
        false
      )
    ).thenReturn(created);

    // Act
    var response = controller
      .createWebhook(
        auth,
        new CreateWebhookCommand(
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          SETTINGS,
          false
        )
      )
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    verify(repository).create(
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      SETTINGS,
      false
    );
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    auth = new ClientAuthentication("user", Map.of());

    // Act
    var response = controller
      .createWebhook(
        auth,
        new CreateWebhookCommand(
          "https://example.com/hook",
          Webhook.Type.DISCORD,
          SETTINGS,
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
      any(),
      anyBoolean()
    );
  }
}
