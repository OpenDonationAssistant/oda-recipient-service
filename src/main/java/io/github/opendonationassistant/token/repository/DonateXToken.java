package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

public class DonateXToken extends GenericToken {

  public DonateXToken(TokenData data, TokenDataRepository repository) {
    super(data, repository);
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
