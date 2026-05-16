package io.github.opendonationassistant.token.repository;

import java.util.concurrent.CompletableFuture;

public interface Token {
  public TokenData data();
  public void save();
  public void toggle();
  public CompletableFuture<Void> delete();
}
