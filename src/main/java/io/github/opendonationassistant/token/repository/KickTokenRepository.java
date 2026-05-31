package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.kick.KickClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class KickTokenRepository implements TokenProvider<KickToken, KickToken.Settings> {

  private static final String SYSTEM = "Kick";
  private final TokenDataRepository repository;
  private final KickClient client;
  private final RabbitClient rabbit;

  @Inject
  public KickTokenRepository(
    TokenDataRepository repository,
    KickClient client,
    @Named("commands") RabbitClient rabbit
  ) {
    this.repository = repository;
    this.client = client;
    this.rabbit = rabbit;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  public CompletableFuture<KickToken> create(
    String token,
    String recipientId,
    KickToken.Settings settings
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

  public Optional<KickToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public KickToken convert(TokenData data) {
    return new KickToken(client, data, repository, rabbit);
  }
}
