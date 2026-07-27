package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.goodgame.GoodGameClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class GoodGameTokenRepository
  extends GenericTokenProvider<GoodGameToken, GoodGameToken.Settings> {

  private final RabbitClient events;
  private final GoodGameClient client;

  @Inject
  public GoodGameTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    GoodGameClient client
  ) {
    super(repository);
    this.events = events;
    this.client = client;
  }

  @Override
  public String system() {
    return "GoodGame";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public GoodGameToken convert(TokenData data) {
    return new GoodGameToken(client, data, repository, events);
  }
}
