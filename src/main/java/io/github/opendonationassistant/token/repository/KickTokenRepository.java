package io.github.opendonationassistant.token.repository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

import io.github.opendonationassistant.integration.kick.KickClient;

@Singleton
public class KickTokenRepository implements TokenProvider<KickToken> {

  private final TokenDataRepository repository;
  private final KickClient client;

  @Inject
  public KickTokenRepository(TokenDataRepository repository, KickClient client) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return "Kick";
  }

  public Optional<KickToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public KickToken convert(TokenData data) {
    return new KickToken(client, data, repository);
  }
}
