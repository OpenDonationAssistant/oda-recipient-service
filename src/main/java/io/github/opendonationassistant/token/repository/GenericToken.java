package io.github.opendonationassistant.token.repository;

import java.util.HashMap;
import java.util.Map;

public class GenericToken implements Token {

  private final TokenData data;
  private final TokenDataRepository repository;

  public GenericToken(TokenData data, TokenDataRepository repository) {
    var mergedSettings = defaultSettings();
    mergedSettings.putAll(data.settings());
    this.data = new TokenData(
      data.id(),
      data.token(),
      data.type(),
      data.recipientId(),
      data.system(),
      data.enabled(),
      mergedSettings
    );
    this.repository = repository;
  }

  @Override
  public TokenData data() {
    return this.data();
  }

  @Override
  public void save() {
    this.repository.update(data);
  }

  @Override
  public void toggle() {}

  protected Map<String, Object> defaultSettings() {
    return new HashMap<>();
  }
}
