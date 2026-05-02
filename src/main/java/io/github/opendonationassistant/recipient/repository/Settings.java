package io.github.opendonationassistant.recipient.repository;

import io.github.opendonationassistant.recipient.repository.SettingsData.Feature;
import java.util.List;

public class Settings {

  private SettingsData data;
  private SettingsDataRepository repository;

  public Settings(SettingsData data, SettingsDataRepository repository) {
    this.data = data;
    this.repository = repository;
  }

  public SettingsData data() {
    return this.data;
  }

  public void setFeatureStatus(String name, SettingsData.FeatureStatus status) {
    List<Feature> updatedFeatures = data
      .features()
      .stream()
      .filter(f -> !f.name().equals(name))
      .toList();
    updatedFeatures.add(new Feature(name, status));
    this.data = new SettingsData(
      data.id(),
      data.recipientId(),
      updatedFeatures,
      data.logLevels()
    );
    save();
  }

  public void save() {
    repository.update(data);
  }
}
