package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class UnofficialDonationAlertsTokenRepository
  extends GenericTokenProvider<
    UnofficialDonationAlertsToken,
    UnofficialDonationAlertsToken.Settings
  > {

  private final RabbitClient events;

  @Inject
  public UnofficialDonationAlertsTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    super(repository);
    this.events = events;
  }

  @Override
  public String system() {
    return "UnofficialDonationAlerts";
  }

  @Override
  public UnofficialDonationAlertsToken convert(TokenData data) {
    return new UnofficialDonationAlertsToken(data, repository, events);
  }

  @Override
  public String getType() {
    return "accessToken";
  }
}
