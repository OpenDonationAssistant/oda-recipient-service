package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.vklive.VKLiveClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class VkliveTokenRepository implements TokenProvider<VkliveToken> {

  private final TokenDataRepository repository;
  private final VKLiveClient client;
  private final RabbitClient rabbit;

  @Inject
  public VkliveTokenRepository(
    TokenDataRepository repository,
    VKLiveClient client,
    RabbitClient rabbit
  ) {
    this.repository = repository;
    this.client = client;
    this.rabbit = rabbit;
  }

  @Override
  public String system() {
    return "Vklive";
  }

  public Optional<VkliveToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public VkliveToken convert(TokenData data) {
    return new VkliveToken(client, data, repository, rabbit);
  }
}
