package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;

public class TributeToken extends GenericToken {

  public TributeToken(TokenData data, TokenDataRepository repository) {
    super(data, repository);
  }

  @Override
  protected Map<String, Object> defaultSettings() {
    var settings = new HashMap<String, Object>();
    settings.put("handleDonations", true);
    settings.put("handleSubscriptions", true);
    settings.put("handlePurchases", true);
    settings.put("triggerAlerts", true);
    settings.put("triggerDonaton", true);
    settings.put("triggerReel", true);
    settings.put("addToGoal", true);
    settings.put("countInTop", true);
    return settings;
  }

  @Serdeable
  public static record Settings(
    boolean handleDonations,
    boolean handleSubscriptions,
    boolean handlePurchases
  )
    implements JsonConvertable {
    @Override
    public Map<String, Object> asJsonMap() {
      // prettier-ignore ON
      return Map.of(
        "handleDonations", handleDonations,
        "handleSubscriptions", handleSubscriptions,
        "handlePurchases", handlePurchases
      );
      // prettier-ignore OFF
    }
  }
}
