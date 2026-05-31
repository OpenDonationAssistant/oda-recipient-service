package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.kick.KickClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class KickTokenRepository implements TokenProvider<KickToken> {

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

  @Override
  public KickToken create(
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

  public Optional<KickToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public KickToken convert(TokenData data) {
    return new KickToken(client, data, repository, rabbit);
  }
}
