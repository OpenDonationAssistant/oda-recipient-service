package io.github.opendonationassistant.token.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
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
  boolean deleted,
  @MappedProperty(type = DataType.JSON) Map<String, Object> settings,
  @MappedProperty(type = DataType.JSON) List<String> scopes
) {
  public TokenData withEnabled(boolean enabled) {
    return new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      enabled,
      deleted,
      settings,
      scopes
    );
  }

  public TokenData withDeleted(boolean deleted) {
    return new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      enabled,
      deleted,
      settings,
      scopes
    );
  }

  public TokenData withToken(String token) {
    return new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      enabled,
      deleted,
      settings,
      scopes
    );
  }

  public TokenData withSettings(Map<String, Object> settings) {
    return new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      enabled,
      deleted,
      settings,
      scopes
    );
  }

  public TokenData withScopes(List<String> scopes) {
    return new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      enabled,
      deleted,
      settings,
      scopes
    );
  }
}
