package io.github.opendonationassistant.recipient.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class SettingsRepository {

  private final SettingsDataRepository repository;
  private final RabbitClient eventsFacade;

  @Inject
  public SettingsRepository(
    SettingsDataRepository repository,
    @Named("events") RabbitClient eventsFacade
  ) {
    this.repository = repository;
    this.eventsFacade = eventsFacade;
  }

  public Settings get(String recipientId) {
    return new Settings(
      repository
        .findByRecipientId(recipientId)
        .orElseGet(() -> defaultSettings(recipientId)),
      repository,
      eventsFacade
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
