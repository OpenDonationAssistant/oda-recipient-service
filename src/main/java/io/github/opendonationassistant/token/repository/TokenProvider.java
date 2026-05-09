package io.github.opendonationassistant.token.repository;

public interface TokenProvider<T extends GenericToken> {
  String system();
  T convert(TokenData data);
}
