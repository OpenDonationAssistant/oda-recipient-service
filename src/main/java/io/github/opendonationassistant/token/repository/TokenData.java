package io.github.opendonationassistant.token.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
@MappedEntity("token")
public record TokenData(
  @Id String id,
  String token,
  String type,
  String recipientId,
  String system,
  boolean enabled,
  @MappedProperty(type = DataType.JSON) Map<String, Object> settings
) {
  public TokenData withEnabled(boolean newValue) {
    return new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      newValue,
      settings
    );
  }
}
