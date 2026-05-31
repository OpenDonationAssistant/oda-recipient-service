package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class TwitchTokenRepository implements TokenProvider<TwitchToken> {

  private static final String SYSTEM = "Twitch";
  private final TokenDataRepository repository;
  private final TwitchClient client;
  private RabbitClient rabbit;

  @Inject
  public TwitchTokenRepository(
    TokenDataRepository repository,
    TwitchClient client,
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
  public TwitchToken create(
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

  public Optional<TwitchToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public TwitchToken convert(TokenData data) {
    return new TwitchToken(client, data, repository, rabbit);
  }
}
