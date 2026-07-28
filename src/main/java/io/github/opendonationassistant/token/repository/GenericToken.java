package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.events.TokenSettingsChanged;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GenericToken implements Token {

  private TokenData data;
  private final TokenDataRepository repository;
  private final RabbitClient events;
  private final ODALogger log = new ODALogger(this);

  public GenericToken(
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    var mergedSettings = defaultSettings();
    mergedSettings.putAll(data.settings());
    this.data = data.withSettings(mergedSettings);
    this.repository = repository;
    this.events = events;
  }

  @Override
  public TokenData data() {
    return this.data;
  }

  @Override
  public void save() {
    try {
      events.sendEvent(
        new TokenSettingsChanged(
          data.id(),
          data.type(),
          data.recipientId(),
          data.system(),
          data.enabled(),
          data.deleted(),
          data.settings()
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info(
      "Saving token",
      Map.of(
        "id",
        data.id(),
        "recipientId",
        data.recipientId(),
        "system",
        data.system()
      )
    );
    if (this.repository.existsById(data.id())) {
      this.repository.update(data);
    } else {
      this.repository.insert(data);
    }
  }

  public void update(String token) {
    this.data = this.data.withToken(token);
    save();
  }

  public void update(Map<String, Object> settings) {
    var updated = new HashMap<>(this.data().settings());
    updated.putAll(settings);
    this.data = this.data.withSettings(updated);
    save();
  }

  @Override
  public void toggle() {
    this.data = this.data.withEnabled(!data.enabled());
    save();
  }

  @Override
  public CompletableFuture<Void> delete() {
    this.data = this.data.withDeleted(true);
    save();
    return CompletableFuture.completedFuture(null);
  }

  protected Map<String, Object> defaultSettings() {
    return new HashMap<>();
  }
}
