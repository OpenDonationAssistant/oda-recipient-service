package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.vklive.VKLiveClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class VkliveTokenRepository
  implements TokenProvider<VkliveToken, VkliveToken.Settings> {

  private static final String SYSTEM = "VKLive";
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
    return SYSTEM;
  }

  @Override
  public CompletableFuture<VkliveToken> create(
    String token,
    String recipientId,
    VkliveToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    var data = new TokenData(
      id,
      token,
      "refreshToken",
      recipientId,
      SYSTEM,
      true,
      false,
      settings.asJsonMap()
    );
    repository.save(data);
    return CompletableFuture.completedFuture(convert(data));
  }

  public Optional<VkliveToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public VkliveToken convert(TokenData data) {
    return new VkliveToken(client, data, repository, rabbit);
  }
}
