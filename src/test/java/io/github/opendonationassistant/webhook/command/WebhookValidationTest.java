package io.github.opendonationassistant.webhook.command;

import static org.junit.jupiter.api.Assertions.*;

import io.github.opendonationassistant.webhook.command.CreateWebhook.CreateWebhookCommand;
import io.github.opendonationassistant.webhook.command.DeleteWebhook.DeleteWebhookCommand;
import io.github.opendonationassistant.webhook.command.UpdateWebhook.UpdateWebhookCommand;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Verifies the declarative Micronaut HTTP validation constraints on the webhook command records.
 * These run in the HTTP/AOP layer, so they cannot be exercised by calling the controller directly.
 * Uses the application {@link Validator} bean so the custom {@code @WebhookUrl} constraint is
 * resolved exactly as it is at runtime.
 */
@MicronautTest(environments = "allinone")
public class WebhookValidationTest {

  private static final String VALID_ID =
    "11111111-1111-1111-1111-111111111111";

  @Inject
  Validator validator;

  @Test
  public void testCreateAcceptsValidCommand() {
    var violations = validator.validate(
      new CreateWebhookCommand(
        "https://example.com/hook",
        Webhook.Type.DISCORD,
        Map.of(),
        true
      )
    );

    assertTrue(violations.isEmpty());
  }

  @Test
  public void testCreateRejectsBlankUrl() {
    var violations = validator.validate(
      new CreateWebhookCommand("   ", Webhook.Type.DISCORD, null, true)
    );

    assertTrue(hasViolation(violations, "url"));
  }

  @Test
  @SuppressWarnings("NullAway")
  public void testCreateRejectsNullUrl() {
    // A client can omit "url"; serde then passes null even though the record is non-null.
    var violations = validator.validate(
      new CreateWebhookCommand(null, Webhook.Type.DISCORD, null, true)
    );

    assertTrue(
      violations
        .stream()
        .anyMatch(violation ->
          violation.getPropertyPath().toString().equals("url") &&
          violation
            .getMessageTemplate()
            .contains("jakarta.validation.constraints.NotBlank")
        ),
      "@NotBlank must reject a null url; @WebhookUrl intentionally allows null for partial updates"
    );
  }

  @Test
  public void testCreateRejectsNonHttpUrl() {
    var violations = validator.validate(
      new CreateWebhookCommand("ftp://example.com/hook", Webhook.Type.DISCORD, null, true)
    );

    assertTrue(hasViolation(violations, "url"));
  }

  @Test
  public void testCreateRejectsPrivateNetworkUrl() {
    var violations = validator.validate(
      new CreateWebhookCommand("http://192.168.1.10/hook", Webhook.Type.DISCORD, null, true)
    );

    assertTrue(hasViolation(violations, "url"));
  }

  @Test
  public void testUpdateAcceptsCommandWithOmittedOptionalFields() {
    var violations = validator.validate(
      new UpdateWebhookCommand(VALID_ID, null, null, null, false)
    );

    assertTrue(violations.isEmpty());
  }

  @Test
  public void testUpdateRejectsBlankId() {
    var violations = validator.validate(
      new UpdateWebhookCommand("   ", "https://example.com/hook", null, null, true)
    );

    assertTrue(hasViolation(violations, "id"));
  }

  @Test
  public void testUpdateRejectsBlankUrlWhenProvided() {
    var violations = validator.validate(
      new UpdateWebhookCommand(VALID_ID, "   ", null, null, true)
    );

    assertTrue(hasViolation(violations, "url"));
  }

  @Test
  public void testUpdateRejectsNonHttpUrlWhenProvided() {
    var violations = validator.validate(
      new UpdateWebhookCommand(VALID_ID, "file:///etc/passwd", null, null, true)
    );

    assertTrue(hasViolation(violations, "url"));
  }

  @Test
  public void testDeleteRejectsBlankId() {
    var violations = validator.validate(new DeleteWebhookCommand("   "));

    assertTrue(hasViolation(violations, "id"));
  }

  private boolean hasViolation(
    Set<? extends ConstraintViolation<?>> violations,
    String property
  ) {
    return violations
      .stream()
      .anyMatch(violation ->
        violation.getPropertyPath().toString().equals(property)
      );
  }
}
