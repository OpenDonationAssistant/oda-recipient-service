package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.kick.KickClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class KickTokenRepository
  extends GenericTokenProvider<KickToken, KickToken.Settings> {

  private final KickClient client;
  private final RabbitClient events;
  private final RabbitClient rabbit;

  @Inject
  public KickTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    KickClient client,
    @Named("commands") RabbitClient rabbit
  ) {
    super(repository);
    this.events = events;
    this.client = client;
    this.rabbit = rabbit;
  }

  @Override
  public String system() {
    return "Kick";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public KickToken convert(TokenData data) {
    return new KickToken(client, data, repository, events, rabbit);
  }
}
