package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.integration.discord.DiscordClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

public class DiscordToken extends RefreshToken {

  public DiscordToken(
    DiscordClient client,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(client, data, repository);
  }

  @Serdeable
  public static record Settings(String id, String username) implements JsonConvertable {

    @Override
    public Map<String, Object> asJsonMap() {
      return Map.of();
    }
  }
}
