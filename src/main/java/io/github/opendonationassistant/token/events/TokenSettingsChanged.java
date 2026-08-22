package io.github.opendonationassistant.token.events;

import io.github.opendonationassistant.events.HasRecipientId;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
public record TokenSettingsChanged(
  String id,
  String type,
  String recipientId,
  String system,
  boolean enabled,
  boolean deleted,
  Map<String, Object> settings,
  Event event
)
  implements HasRecipientId {
  public static enum Event {
    TOKEN_CREATED,
    TOKEN_UPDATED,
    SETTINGS_CHANGED,
    TOKEN_TOGGLED,
    TOKEN_DELETED,
  }
}
