package io.github.opendonationassistant.token.repository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

import io.github.opendonationassistant.integration.twitch.TwitchClient;

@Singleton
public class TwitchTokenRepository implements TokenProvider<TwitchToken> {

  private final TokenDataRepository repository;
  private final TwitchClient client;

  @Inject
  public TwitchTokenRepository(TokenDataRepository repository, TwitchClient client) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return "Twitch";
  }

  public Optional<TwitchToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public TwitchToken convert(TokenData data) {
    return new TwitchToken(client, data, repository);
  }
}
