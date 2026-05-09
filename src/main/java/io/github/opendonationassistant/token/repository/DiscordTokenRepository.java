package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.discord.DiscordClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DiscordTokenRepository implements TokenProvider<DiscordToken> {

  private final TokenDataRepository repository;
  private final DiscordClient client;

  @Inject
  public DiscordTokenRepository(
    TokenDataRepository repository,
    DiscordClient client
  ) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return "Discord";
  }

  public Optional<DiscordToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public DiscordToken convert(TokenData data) {
    return new DiscordToken(client, data, repository);
  }
}
