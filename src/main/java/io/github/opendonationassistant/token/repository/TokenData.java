package io.github.opendonationassistant.token.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("token")
public record TokenData(
  @Id String id,
  String token,
  String type,
  String recipientId,
  String system,
  boolean enabled
) {
  public TokenData withEnabled(boolean enabled) {
    return new TokenData(id, token, type, recipientId, system, enabled);
  }
}
