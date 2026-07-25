package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class TributeTokenRepository
  implements TokenProvider<TributeToken, TributeToken.Settings> {

  private static final String SYSTEM = "Tribute";
  private final TokenDataRepository repository;

  public TributeTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  @Override
  public CompletableFuture<TributeToken> create(
    String token,
    String recipientId,
    TributeToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<TributeToken> create(
    String id,
    String token,
    String recipientId,
    Map<String, Object> settings
  ) {
    var data = new TokenData(
      id,
      token,
      "accessToken",
      recipientId,
      SYSTEM,
      true,
      false,
      settings
    );
    repository.save(data);
    return CompletableFuture.completedFuture(convert(data));
  }

  public Optional<TributeToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public TributeToken convert(TokenData data) {
    return new TributeToken(data, repository);
  }
}
