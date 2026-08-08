package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;

public class IHAQToken extends RefreshToken {

  public IHAQToken(
    OauthClient client,
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    super(client, data, repository, events);
  }

  @Override
  protected Map<String, Object> defaultSettings() {
    var settings = new HashMap<String, Object>();
    settings.put("id", "");
    settings.put("username", "");
    settings.put("apiToken", "");
    settings.put("triggerAlerts", true);
    settings.put("triggerDonaton", true);
    settings.put("triggerReel", true);
    settings.put("addToGoal", true);
    settings.put("countInTop", true);
    return settings;
  }

  @Serdeable
  public record Settings(
    String id,
    String username,
    String apiToken,
    boolean triggerAlerts,
    boolean triggerDonaton,
    boolean triggerReel,
    boolean addToGoal,
    boolean countInTop
  )
    implements JsonConvertable {
    public Settings(String id, String username, String apiToken) {
      this(id, username, apiToken, true, true, true, true, true);
    }

    public Map<String, Object> asJsonMap() {
      // prettier-ignore ON
      return Map.of(
        "id", id,
        "username", username,
        "apiToken", apiToken,
        "triggerAlerts", triggerAlerts,
        "triggerDonaton", triggerDonaton,
        "triggerReel", triggerReel,
        "addToGoal", addToGoal,
        "countInTop", countInTop
      );
      // prettier-ignore OFF
    }
  }
}
