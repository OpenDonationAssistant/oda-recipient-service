package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class TributeTokenRepository
  extends GenericTokenProvider<TributeToken, TributeToken.Settings> {

  private final RabbitClient events;

  public TributeTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    super(repository);
    this.events = events;
  }

  @Override
  public String system() {
    return "Tribute";
  }

  @Override
  public String getType() {
    return "accessToken";
  }

  public TributeToken convert(TokenData data) {
    return new TributeToken(data, repository, events);
  }
}
