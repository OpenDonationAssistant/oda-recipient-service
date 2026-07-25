package io.github.opendonationassistant.recipient.repository;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.recipient.repository.SettingsData.Feature;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Settings {

  private ODALogger log = new ODALogger(this);
  private SettingsData data;
  private SettingsDataRepository repository;
  private RabbitClient eventsFacade;

  public Settings(
    SettingsData data,
    SettingsDataRepository repository,
    RabbitClient eventsFacade
  ) {
    this.data = data;
    this.repository = repository;
    this.eventsFacade = eventsFacade;
  }

  public SettingsData data() {
    return this.data;
  }

  public void setFeatureStatus(String name, SettingsData.FeatureStatus status) {
    List<Feature> updatedFeatures = data
      .features()
      .stream()
      .filter(f -> !f.name().equals(name))
      .collect(Collectors.toList());
    updatedFeatures.add(new Feature(name, status));
    this.data = new SettingsData(
      data.id(),
      data.recipientId(),
      updatedFeatures,
      data.logLevels()
    );
    save();
    try {
      List<FeaturesUpdatedEvent.Feature> features = data
        .features()
        .stream()
        .map(f ->
          new FeaturesUpdatedEvent.Feature(
            f.name(),
            FeaturesUpdatedEvent.FeatureStatus.valueOf(f.status().name())
          )
        )
        .toList();
      eventsFacade.sendEvent(
        new FeaturesUpdatedEvent(data.recipientId(), features)
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void save() {
    log.info("Saving settings", Map.of("data", data));
    repository.save(data);
  }
}
