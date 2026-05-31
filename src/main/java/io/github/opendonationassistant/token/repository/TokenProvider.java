package io.github.opendonationassistant.token.repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TokenProvider<T extends GenericToken> {
  CompletableFuture<T> create(
    String token,
    String recipientId,
    Map<String, Object> setting
  );
  String system();
  T convert(TokenData data);
}
