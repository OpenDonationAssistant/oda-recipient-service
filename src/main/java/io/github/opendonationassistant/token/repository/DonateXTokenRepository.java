package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class DonateXTokenRepository
  extends GenericTokenProvider<DonateXToken, DonateXToken.Settings> {

  private final RabbitClient events;

  public DonateXTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    super(repository);
    this.events = events;
  }

  @Override
  public String system() {
    return "DonateX";
  }

  @Override
  public String getType() {
    return "accessToken";
  }

  public DonateXToken convert(TokenData data) {
    return new DonateXToken(data, repository, events);
  }
}
