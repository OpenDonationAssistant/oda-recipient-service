package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TokenProvider<
  T extends GenericToken, S extends JsonConvertable
> {
  CompletableFuture<T> create(String token, String recipientId, S settings);
  CompletableFuture<T> create(
    String id,
    String token,
    String recipientId,
    Map<String, Object> settings
  );
  String system();
  T convert(TokenData data);
}
