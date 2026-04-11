package io.github.opendonationassistant.recipient.repository;

public class Settings {

  private final SettingsData data;

  public Settings(SettingsData data) {
    this.data = data;
  }

  public SettingsData data() {
    return this.data;
  }
}
