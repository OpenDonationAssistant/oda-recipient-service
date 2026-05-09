package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class KickTokenRepository {

  private final TokenDataRepository repository;

  public KickTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public Optional<KickToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public KickToken convert(TokenData data) {
    return new KickToken(data, repository);
  }
}
