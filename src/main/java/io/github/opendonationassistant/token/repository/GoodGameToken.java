package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

public class GoodGameToken extends RefreshToken {

  public GoodGameToken(
    OauthClient client,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(client, data, repository);
  }

  @Serdeable
  public record Settings(String id, String nickname, String avatar)
    implements JsonConvertable {
    public Map<String, Object> asJsonMap() {
      return Map.of("id", id, "nickname", nickname, "avatar", avatar);
    }
  }
}
