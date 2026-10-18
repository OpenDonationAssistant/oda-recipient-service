package io.github.opendonationassistant.webhook.events;

import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
public record WebhookChangedEvent(
  String id,
  String recipientId,
  String url,
  Webhook.Type type,
  Map<String, Object> settings,
  boolean enabled,
  Event event
)
  implements HasRecipientId {
  @Serdeable
  public static enum Event {
    WEBHOOK_CREATED,
    WEBHOOK_UPDATED,
    WEBHOOK_DELETED,
  }
}
