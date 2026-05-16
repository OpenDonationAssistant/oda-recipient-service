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
    this.repository.update(data);
  }

  @Override
  public void toggle() {
    this.data = this.data.withEnabled(!data.enabled());
    save();
  }

  @Override
  public CompletableFuture<Void> delete() {
    return CompletableFuture.runAsync(() -> {
      log.info(
        "Deleting token",
        Map.of("id", data.id(), "recipientId", data.recipientId())
      );
      repository.delete(data);
    });
  }

  protected Map<String, Object> defaultSettings() {
    return new HashMap<>();
  }
}
