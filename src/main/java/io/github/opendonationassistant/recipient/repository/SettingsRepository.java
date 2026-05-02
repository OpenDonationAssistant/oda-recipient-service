package io.github.opendonationassistant.recipient.repository;

import com.fasterxml.uuid.Generators;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class SettingsRepository {

  private final SettingsDataRepository repository;

  @Inject
  public SettingsRepository(SettingsDataRepository repository) {
    this.repository = repository;
  }

  public Settings get(String recipientId) {
    return new Settings(
      repository
        .findByRecipientId(recipientId)
        .orElseGet(() -> defaultSettings(recipientId)),
      repository
    );
  }

  private SettingsData defaultSettings(String recipientId) {
    return new SettingsData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      recipientId,
      List.of(),
      List.of()
    );
  }
}
