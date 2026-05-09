package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DiscordTokenRepository {

  private final TokenDataRepository repository;

  public DiscordTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public Optional<DiscordToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public DiscordToken convert(TokenData data) {
    return new DiscordToken(data, repository);
  }
}
