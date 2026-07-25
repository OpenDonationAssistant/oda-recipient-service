package io.github.opendonationassistant.token.repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Token {
  public TokenData data();
  public void save();
  public void update(String token);
  public void update(Map<String, Object> settings);
  public void toggle();
  public CompletableFuture<Void> delete();
}
