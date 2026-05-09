package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.vklive.VKLiveClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class VkliveTokenRepository implements TokenProvider<VkliveToken> {

  private final TokenDataRepository repository;
  private final VKLiveClient client;

  @Inject
  public VkliveTokenRepository(
    TokenDataRepository repository,
    VKLiveClient client
  ) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return "Vklive";
  }

  public Optional<VkliveToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public VkliveToken convert(TokenData data) {
    return new VkliveToken(client, data, repository);
  }
}
