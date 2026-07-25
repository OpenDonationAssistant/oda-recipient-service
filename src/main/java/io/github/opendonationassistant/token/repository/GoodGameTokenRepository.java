package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.goodgame.GoodGameClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class GoodGameTokenRepository
  implements TokenProvider<GoodGameToken, GoodGameToken.Settings> {

  private static final String SYSTEM = "GoodGame";
  private final TokenDataRepository repository;
  private final GoodGameClient client;

  @Inject
  public GoodGameTokenRepository(
    TokenDataRepository repository,
    GoodGameClient client
  ) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  @Override
  public CompletableFuture<GoodGameToken> create(
    String token,
    String recipientId,
    GoodGameToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<GoodGameToken> create(
    String id,
    String token,
    String recipientId,
    Map<String, Object> settings
  ) {
    var data = new TokenData(
      id,
      token,
      "refreshToken",
      recipientId,
      SYSTEM,
      true,
      false,
      settings
    );
    var created = convert(data);
    created.save();
    return CompletableFuture.completedFuture(created);
  }

  public Optional<GoodGameToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public GoodGameToken convert(TokenData data) {
    return new GoodGameToken(client, data, repository);
  }
}
