package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DonationAlertsTokenRepository implements TokenProvider<DonationAlertsToken> {

  private final TokenDataRepository repository;

  public DonationAlertsTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Override
  public String system() {
    return "DonationAlerts";
  }

  public Optional<DonationAlertsToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public DonationAlertsToken convert(TokenData data) {
    return new DonationAlertsToken(data, repository);
  }
}
