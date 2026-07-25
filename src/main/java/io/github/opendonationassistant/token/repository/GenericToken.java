package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.commons.logging.ODALogger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GenericToken implements Token {

  private TokenData data;
  private final TokenDataRepository repository;
  private ODALogger log = new ODALogger(this);

  public GenericToken(TokenData data, TokenDataRepository repository) {
    var mergedSettings = defaultSettings();
    mergedSettings.putAll(data.settings());
    this.data = data.withSettings(mergedSettings);
    this.repository = repository;
  }

  @Override
  public TokenData data() {
    return this.data;
  }

  @Override
  public void save() {
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
    this.repository.save(data);
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
    return CompletableFuture.runAsync(() -> {
      log.info(
        "Deleting token",
        Map.of("id", data.id(), "recipientId", data.recipientId())
      );
      repository.update(this.data);
    });
  }

  protected Map<String, Object> defaultSettings() {
    return new HashMap<>();
  }
}
