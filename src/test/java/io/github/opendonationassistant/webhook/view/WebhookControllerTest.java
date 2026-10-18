package io.github.opendonationassistant.webhook.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.security.authentication.Authentication;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Pure Mockito unit tests for {@link WebhookController}. The owner is resolved from the JWT and only
 * that recipient's webhooks are returned.
 */
public class WebhookControllerTest {

  WebhookRepository repository = mock(WebhookRepository.class);
  WebhookController controller = new WebhookController(repository);
  Authentication auth = mock(Authentication.class);

  @Test
  public void testListsOwnerWebhooksMappedToDtos() {
    // Arrange
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "recipientId")
    );
    when(repository.findByRecipientId("recipientId")).thenReturn(
      List.of(
        new Webhook(
          "id1",
          "recipientId",
          "https://one.example.com",
          Webhook.Type.DISCORD,
          Map.of("channel", "general"),
          true,
          false
        ),
        new Webhook(
          "id2",
          "recipientId",
          "https://two.example.com",
          Webhook.Type.TELEGRAM,
          Map.of("chatId", "12345"),
          false,
          false
        )
      )
    );

    // Act
    var response = controller.listWebhooks(auth);

    // Assert
    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals(2, body.size());
    assertEquals("id1", body.get(0).id());
    assertEquals("recipientId", body.get(0).recipientId());
    assertEquals("https://one.example.com", body.get(0).url());
    assertEquals(Webhook.Type.DISCORD, body.get(0).type());
    assertEquals(Map.of("channel", "general"), body.get(0).settings());
    assertTrue(body.get(0).enabled());
    assertEquals("id2", body.get(1).id());
    assertEquals("https://two.example.com", body.get(1).url());
    assertEquals(Webhook.Type.TELEGRAM, body.get(1).type());
    assertEquals(Map.of("chatId", "12345"), body.get(1).settings());
    assertFalse(body.get(1).enabled());
    verify(repository).findByRecipientId("recipientId");
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    when(auth.getAttributes()).thenReturn(Map.of());

    // Act
    var response = controller.listWebhooks(auth);

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).findByRecipientId(anyString());
  }
}
