package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class VkliveTokenRepository {

  private final TokenDataRepository repository;

  public VkliveTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public Optional<VkliveToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public VkliveToken convert(TokenData data) {
    return new VkliveToken(data, repository);
  }
}
