package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.HashMap;
import java.util.Map;

public class TributeToken extends GenericToken {

  public TributeToken(
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    super(data, repository, events);
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
    settings.put("subscriptionsTriggerAlerts", false);
    settings.put("subscriptionsTriggerDonaton", false);
    settings.put("subscriptionsTriggerReel", false);
    settings.put("subscriptionsAddToGoal", false);
    settings.put("subscriptionsCountInTop", false);
    settings.put("purchasesTriggerAlerts", false);
    settings.put("purchasesTriggerDonaton", false);
    settings.put("purchasesTriggerReel", false);
    settings.put("purchasesAddToGoal", false);
    settings.put("purchasesCountInTop", false);
    return settings;
  }

  @Serdeable
  public static record Settings(
    boolean handleDonations,
    boolean handleSubscriptions,
    boolean handlePurchases,
    boolean triggerAlerts,
    boolean triggerDonaton,
    boolean triggerReel,
    boolean addToGoal,
    boolean countInTop,
    boolean subscriptionsTriggerAlerts,
    boolean subscriptionsTriggerDonaton,
    boolean subscriptionsTriggerReel,
    boolean subscriptionsAddToGoal,
    boolean subscriptionsCountInTop,
    boolean purchasesTriggerAlerts,
    boolean purchasesTriggerDonaton,
    boolean purchasesTriggerReel,
    boolean purchasesAddToGoal,
    boolean purchasesCountInTop
  )
    implements JsonConvertable {
    @Override
    public Map<String, Object> asJsonMap() {
      var map = new HashMap<String, Object>();
      map.put("handleDonations", handleDonations);
      map.put("handleSubscriptions", handleSubscriptions);
      map.put("handlePurchases", handlePurchases);
      map.put("triggerAlerts", triggerAlerts);
      map.put("triggerDonaton", triggerDonaton);
      map.put("triggerReel", triggerReel);
      map.put("addToGoal", addToGoal);
      map.put("countInTop", countInTop);
      map.put("subscriptionsTriggerAlerts", subscriptionsTriggerAlerts);
      map.put("subscriptionsTriggerDonaton", subscriptionsTriggerDonaton);
      map.put("subscriptionsTriggerReel", subscriptionsTriggerReel);
      map.put("subscriptionsAddToGoal", subscriptionsAddToGoal);
      map.put("subscriptionsCountInTop", subscriptionsCountInTop);
      map.put("purchasesTriggerAlerts", purchasesTriggerAlerts);
      map.put("purchasesTriggerDonaton", purchasesTriggerDonaton);
      map.put("purchasesTriggerReel", purchasesTriggerReel);
      map.put("purchasesAddToGoal", purchasesAddToGoal);
      map.put("purchasesCountInTop", purchasesCountInTop);
      return map;
    }
  }
}
