package io.github.opendonationassistant.webhook.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("webhook")
public record Webhook(
  @Id @MappedProperty(type = DataType.UUID) String id,
  String recipientId,
  String url,
  Type type,
  boolean enabled
) {
  @Serdeable
  public static enum Type {
    DISCORD,
    TELEGRAM,
  }
}
