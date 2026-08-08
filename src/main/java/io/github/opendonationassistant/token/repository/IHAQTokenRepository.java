package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.ihaq.IHAQClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class IHAQTokenRepository
  extends GenericTokenProvider<IHAQToken, IHAQToken.Settings> {

  private final RabbitClient events;
  private final IHAQClient client;

  @Inject
  public IHAQTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    IHAQClient client
  ) {
    super(repository);
    this.events = events;
    this.client = client;
  }

  @Override
  public String system() {
    return "IHAQ";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public IHAQToken convert(TokenData data) {
    return new IHAQToken(client, data, repository, events);
  }
}