package io.github.opendonationassistant.recipient.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
@MappedEntity("settings")
public record SettingsData(
  @Id String id,
  String recipientId,
  @MappedProperty(type = DataType.JSON) List<Feature> features,
  @MappedProperty(type = DataType.JSON) List<LogLevels> logLevels
) {
  @Serdeable
  public static record Feature(String name, FeatureStatus status) {}

  @Serdeable
  public static enum FeatureStatus {
    ENABLED,
    DISABLED,
  }

  @Serdeable
  public static record LogLevels(String name, LogLevel level) {}

  @Serdeable
  public static enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    DISABLED,
  }
}
