package io.github.opendonationassistant.token.repository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.rabbit.RabbitClient;

@Singleton
public class TwitchTokenRepository implements TokenProvider<TwitchToken> {

  private final TokenDataRepository repository;
  private final TwitchClient client;
  private RabbitClient rabbit;


  @Inject
  public TwitchTokenRepository(TokenDataRepository repository, TwitchClient client, RabbitClient rabbit) {
    this.repository = repository;
    this.client = client;
    this.rabbit = rabbit;
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
    return new TwitchToken(client, data, repository, rabbit);
  }
}
