package io.github.opendonationassistant.webhook.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
@MappedEntity("webhook")
public record Webhook(
  @Id @MappedProperty(type = DataType.UUID) String id,
  String recipientId,
  String url,
  Type type,
  @MappedProperty(type = DataType.JSON) Map<String, Object> settings,
  boolean enabled,
  boolean deleted
) {
  public Webhook withDeleted(boolean deleted) {
    return new Webhook(
      id,
      recipientId,
      url,
      type,
      settings,
      enabled,
      deleted
    );
  }

  @Serdeable
  public static enum Type {
    DISCORD,
    TELEGRAM,
  }
}
