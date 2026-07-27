package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class DonationAlertsTokenRepository
  extends GenericTokenProvider<
    DonationAlertsToken,
    DonationAlertsToken.Settings
  > {

  private final RabbitClient events;

  public DonationAlertsTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    super(repository);
    this.events = events;
  }

  @Override
  public String system() {
    return "DonationAlerts";
  }

  @Override
  public String getType() {
    return "accessToken";
  }

  public DonationAlertsToken convert(TokenData data) {
    return new DonationAlertsToken(data, repository, events);
  }
}
