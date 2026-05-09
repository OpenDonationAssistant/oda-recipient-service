package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class TwitchTokenRepository {

  private final TokenDataRepository repository;

  public TwitchTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public Optional<TwitchToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  private TwitchToken convert(TokenData data) {
    return new TwitchToken(data, repository);
  }
}
