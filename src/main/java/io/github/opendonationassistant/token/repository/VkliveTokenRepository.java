package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.vklive.VKLiveClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class VkliveTokenRepository
  extends GenericTokenProvider<VkliveToken, VkliveToken.Settings> {

  private final VKLiveClient client;
  private final RabbitClient events;
  private final RabbitClient rabbit;

  @Inject
  public VkliveTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    VKLiveClient client,
    @Named("commands") RabbitClient rabbit
  ) {
    super(repository);
    this.events = events;
    this.client = client;
    this.rabbit = rabbit;
  }

  @Override
  public String system() {
    return "VKLive";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public VkliveToken convert(TokenData data) {
    return new VkliveToken(client, data, repository, events, rabbit);
  }
}
