package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.discord.DiscordClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class DiscordTokenRepository implements TokenProvider<DiscordToken> {

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
  public DiscordToken create(
    String token,
    String recipientId,
    Map<String, Object> settings
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
      settings
    );
    repository.save(data);
    return convert(data);
  }

  public Optional<DiscordToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public DiscordToken convert(TokenData data) {
    return new DiscordToken(client, data, repository);
  }
}
