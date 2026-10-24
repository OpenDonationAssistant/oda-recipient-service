package io.github.opendonationassistant.webhook.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a webhook target is an absolute {@code http}/{@code https} URL pointing at a
 * public host. Blocks loopback, private, link-local and CGNAT literal addresses as well as the
 * {@code localhost} family, so user-supplied webhooks cannot target cluster-internal services or
 * cloud metadata endpoints.
 *
 * <p>Hostnames are not resolved here: a public hostname that resolves to a private address (or DNS
 * rebinding) is not detected. Consumers performing the outbound call must still enforce a network
 * policy.
 */
@Documented
@Constraint(validatedBy = WebhookUrlValidator.class)
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.RECORD_COMPONENT,
  ElementType.ANNOTATION_TYPE,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebhookUrl {
  String message() default
    "must be an absolute http(s) URL pointing at a public host";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
