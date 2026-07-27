package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class TwitchTokenRepository
  extends GenericTokenProvider<TwitchToken, TwitchToken.Settings> {

  private final TwitchClient client;
  private final RabbitClient events;
  private final RabbitClient rabbit;

  @Inject
  public TwitchTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    TwitchClient client,
    @Named("commands") RabbitClient rabbit
  ) {
    super(repository);
    this.events = events;
    this.client = client;
    this.rabbit = rabbit;
  }

  @Override
  public String system() {
    return "Twitch";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public TwitchToken convert(TokenData data) {
    return new TwitchToken(client, data, repository, events, rabbit);
  }
}
