package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DonateXTokenRepository {

  private final TokenDataRepository repository;

  public DonateXTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public Optional<DonateXToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public DonateXToken convert(TokenData data) {
    return new DonateXToken(data, repository);
  }
}
