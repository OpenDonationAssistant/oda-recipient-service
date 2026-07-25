package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.streamlabs.StreamlabsClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class StreamlabsTokenRepository
  implements TokenProvider<StreamlabsToken, StreamlabsToken.Settings> {

  private static final String SYSTEM = "Streamlabs";
  private final TokenDataRepository repository;
  private final StreamlabsClient client;

  @Inject
  public StreamlabsTokenRepository(
    TokenDataRepository repository,
    StreamlabsClient client
  ) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  @Override
  public CompletableFuture<StreamlabsToken> create(
    String token,
    String recipientId,
    StreamlabsToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<StreamlabsToken> create(
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

  public Optional<StreamlabsToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public StreamlabsToken convert(TokenData data) {
    return new StreamlabsToken(client, data, repository);
  }
}
