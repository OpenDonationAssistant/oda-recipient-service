package io.github.opendonationassistant.token.repository;

import java.util.HashMap;
import java.util.Map;

public class DonationAlertsToken extends GenericToken {

  public DonationAlertsToken(TokenData data, TokenDataRepository repository) {
    super(data, repository);
  }

  @Override
  protected Map<String, Object> defaultSettings() {
    var settings = new HashMap<String, Object>();
    settings.put("triggerAlerts", true);
    settings.put("triggerDonatofon", true);
    settings.put("addToGoal", true);
    settings.put("countInTop", true);
    return settings;
  }
}
