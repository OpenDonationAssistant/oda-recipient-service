package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;

public class StreamElementsJWTToken extends GenericToken {

  public StreamElementsJWTToken(
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    super(data, repository, events);
  }
}
