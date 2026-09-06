package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;

public class GoogleApiKeyToken extends GenericToken {

  public GoogleApiKeyToken(
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    super(data, repository, events);
  }

  @Override
  protected Map<String, Object> defaultSettings() {
    var settings = new HashMap<String, Object>();
    return settings;
  }

  @Serdeable
  public static record Settings() implements JsonConvertable {
    @Override
    public Map<String, Object> asJsonMap() {
      // prettier-ignore ON
      return Map.of(
      );
      // prettier-ignore OFF
    }
  }
}

