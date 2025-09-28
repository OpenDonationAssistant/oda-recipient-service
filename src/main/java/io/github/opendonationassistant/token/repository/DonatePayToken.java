package io.github.opendonationassistant.token.repository;

import java.util.HashMap;
import java.util.Map;

public class DonatePayToken extends GenericToken {

  public DonatePayToken(TokenData data, TokenDataRepository repository) {
    super(data, repository);
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
}
