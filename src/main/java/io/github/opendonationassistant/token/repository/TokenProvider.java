package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import java.util.concurrent.CompletableFuture;

public interface TokenProvider<
  T extends GenericToken, S extends JsonConvertable
> {
  CompletableFuture<T> create(String token, String recipientId, S settings);
  String system();
  T convert(TokenData data);
}
