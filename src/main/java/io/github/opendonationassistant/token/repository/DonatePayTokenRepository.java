package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DonatePayTokenRepository {

  private final TokenDataRepository repository;

  public DonatePayTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public Optional<DonatePayToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public DonatePayToken convert(TokenData data) {
    return new DonatePayToken(data, repository);
  }
}
