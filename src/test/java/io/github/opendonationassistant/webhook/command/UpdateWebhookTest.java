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
import org.mockito.ArgumentCaptor;

/**
 * Pure Mockito unit tests for {@link UpdateWebhook}. Partial-update semantics: omitted fields are
 * preserved. Missing, non-owner and a lost update all collapse to 401 (the caller cannot distinguish
 * existence). Request-body constraints are covered by {@link WebhookValidationTest}.
 */
public class UpdateWebhookTest {

  private static final String ID = "11111111-1111-1111-1111-111111111111";

  WebhookRepository repository = mock(WebhookRepository.class);
  UpdateWebhook controller = new UpdateWebhook(repository);
  Authentication auth = mock(Authentication.class);

  private void authenticatedAs(String recipientId) {
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", recipientId)
    );
  }

  @Test
  public void testUpdatesWebhookOwnedByAuthenticatedRecipient() {
    // Arrange
    authenticatedAs("recipientId");
    var existing = new Webhook(
      ID,
      "recipientId",
      "https://example.com/old",
      Webhook.Type.TELEGRAM,
      Map.of("chatId", "old"),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(existing));
    when(repository.update(any(Webhook.class))).thenAnswer(inv ->
      Optional.of(inv.getArgument(0))
    );

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(
          ID,
          "https://example.com/new",
          Webhook.Type.TELEGRAM,
          Map.of("chatId", "12345"),
          false
        )
      )
      .join();

    // Assert
    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals("https://example.com/new", body.url());
    assertEquals(Webhook.Type.TELEGRAM, body.type());
    assertEquals(Map.of("chatId", "12345"), body.settings());
    assertFalse(body.enabled());
  }

  @Test
  public void testAppliesPartialUpdatePreservingOmittedFields() {
    // Arrange
    authenticatedAs("recipientId");
    var existing = new Webhook(
      ID,
      "recipientId",
      "https://example.com/old",
      Webhook.Type.TELEGRAM,
      Map.of("chatId", "old"),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(existing));
    when(repository.update(any(Webhook.class))).thenAnswer(inv ->
      Optional.of(inv.getArgument(0))
    );

    // Act - only enabled is provided
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(ID, null, null, null, false)
      )
      .join();

    // Assert - everything except enabled is preserved
    assertEquals(200, response.getStatus().getCode());
    var captor = ArgumentCaptor.forClass(Webhook.class);
    verify(repository).update(captor.capture());
    var merged = captor.getValue();
    assertEquals("https://example.com/old", merged.url());
    assertEquals(Webhook.Type.TELEGRAM, merged.type());
    assertEquals(Map.of("chatId", "old"), merged.settings());
    assertFalse(merged.enabled());
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty() {
    // Arrange
    when(auth.getAttributes()).thenReturn(Map.of());

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(ID, "https://example.com/hook", null, null, true)
      )
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).findById(anyString());
  }

  @Test
  public void testReturnsUnauthorizedWhenWebhookDoesNotExist() {
    // Arrange
    authenticatedAs("recipientId");
    when(repository.findById(ID)).thenReturn(Optional.empty());

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(ID, "https://example.com/hook", null, null, true)
      )
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).update(any(Webhook.class));
  }

  @Test
  public void testReturnsUnauthorizedWhenWebhookOwnedByAnotherRecipient() {
    // Arrange
    authenticatedAs("recipientId");
    var existing = new Webhook(
      ID,
      "otherRecipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(existing));

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(ID, "https://example.com/hook", null, null, true)
      )
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
    verify(repository, never()).update(any(Webhook.class));
  }

  @Test
  public void testReturnsUnauthorizedWhenUpdateReportsLostRow() {
    // Arrange
    authenticatedAs("recipientId");
    var existing = new Webhook(
      ID,
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of(),
      true,
      false
    );
    when(repository.findById(ID)).thenReturn(Optional.of(existing));
    when(repository.update(any(Webhook.class))).thenReturn(Optional.empty());

    // Act
    var response = controller
      .updateWebhook(
        auth,
        new UpdateWebhookCommand(ID, "https://example.com/hook", null, null, true)
      )
      .join();

    // Assert
    assertEquals(401, response.getStatus().getCode());
  }
}
