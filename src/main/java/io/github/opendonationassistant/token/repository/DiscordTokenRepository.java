package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.discord.DiscordClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class DiscordTokenRepository
  extends GenericTokenProvider<DiscordToken, DiscordToken.Settings> {

  private final RabbitClient events;
  private final DiscordClient client;

  @Inject
  public DiscordTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    DiscordClient client
  ) {
    super(repository);
    this.events = events;
    this.client = client;
  }

  @Override
  public String system() {
    return "Discord";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public DiscordToken convert(TokenData data) {
    return new DiscordToken(client, data, repository, events);
  }
}
