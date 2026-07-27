package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;

public class DonationAlertsToken extends GenericToken {

  public DonationAlertsToken(
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    super(data, repository, events);
  }

  @Override
  protected Map<String, Object> defaultSettings() {
    var settings = new HashMap<String, Object>();
    settings.put("triggerAlerts", true);
    settings.put("triggerDonaton", true);
    settings.put("triggerReel", true);
    settings.put("addToGoal", true);
    settings.put("countInTop", true);
    return settings;
  }

  @Serdeable
  public static record Settings(
    boolean triggerAlerts,
    boolean triggerDonaton,
    boolean triggerReel,
    boolean addToGoal,
    boolean countInTop
  )
    implements JsonConvertable {
    @Override
    public Map<String, Object> asJsonMap() {
      // prettier-ignore ON
      return Map.of(
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
