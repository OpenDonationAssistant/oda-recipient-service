package io.github.opendonationassistant.recipient.repository;

import io.github.opendonationassistant.events.HasRecipientId;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record FeaturesUpdatedEvent(String recipientId, List<Feature> features)
  implements HasRecipientId {
  @Serdeable
  public static record Feature(String name, FeatureStatus status) {}

  @Serdeable
  public static enum FeatureStatus {
    ENABLED,
    DISABLED,
  }
}
