package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

public class StreamElementsToken extends RefreshToken {

  public StreamElementsToken(
    OauthClient client,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(client, data, repository);
  }

  @Serdeable
  public record Settings(
    String id,
    String username,
    String displayName,
    String avatar
  ) implements JsonConvertable {
    public Map<String, Object> asJsonMap() {
      return Map.of(
        "id",
        id,
        "username",
        username,
        "displayName",
        displayName,
        "avatar",
        avatar
      );
    }
  }
}
