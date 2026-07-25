package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.discord.DiscordClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DiscordTokenRepository
  implements TokenProvider<DiscordToken, DiscordToken.Settings> {

  private static final String SYSTEM = "Discord";
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
    return SYSTEM;
  }

  @Override
  public CompletableFuture<DiscordToken> create(
    String token,
    String recipientId,
    DiscordToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<DiscordToken> create(
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

  public Optional<DiscordToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public DiscordToken convert(TokenData data) {
    return new DiscordToken(client, data, repository);
  }
}
