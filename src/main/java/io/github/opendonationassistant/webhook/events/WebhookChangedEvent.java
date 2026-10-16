package io.github.opendonationassistant.webhook.events;

import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.webhook.repository.Webhook.Type;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record WebhookChangedEvent(
  String id,
  String recipientId,
  String url,
  Type type,
  boolean enabled,
  Event event
)
  implements HasRecipientId {
  @Serdeable
  public static enum Event {
    WEBHOOK_CREATED,
    WEBHOOK_UPDATED,
  }
}
