package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.streamelements.StreamElementsClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class StreamElementsTokenRepository
  implements TokenProvider<StreamElementsToken, StreamElementsToken.Settings> {

  private static final String SYSTEM = "StreamElements";
  private final TokenDataRepository repository;
  private final StreamElementsClient client;

  @Inject
  public StreamElementsTokenRepository(
    TokenDataRepository repository,
    StreamElementsClient client
  ) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  @Override
  public CompletableFuture<StreamElementsToken> create(
    String token,
    String recipientId,
    StreamElementsToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<StreamElementsToken> create(
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
    repository.save(data);
    return CompletableFuture.completedFuture(convert(data));
  }

  public Optional<StreamElementsToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public StreamElementsToken convert(TokenData data) {
    return new StreamElementsToken(client, data, repository);
  }
}
