package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.integration.kick.KickClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class KickToken extends RefreshToken {

  private final RabbitClient rabbit;

  public KickToken(
    KickClient oauth,
    TokenData data,
    TokenDataRepository repository,
    RabbitClient rabbit
  ) {
    super(oauth, data, repository);
    this.rabbit = rabbit;
  }

  @Override
  public CompletableFuture<Void> delete() {
    return obtainAccessToken()
      .thenCompose(token -> {
        rabbit.sendCommand(
          new UnsubscribeKickEventsCommand(data().recipientId(), data().id())
        );
        rabbit.sendCommand(
          new UnlinkKickAccount(data().recipientId(), data().id())
        );
        return super.delete();
      });
  }

  @Serdeable
  public record Settings(String id, String name, String avatar, String email)
    implements JsonConvertable {
    public Map<String, Object> asJsonMap() {
      return Map.of("id", id, "name", name, "avatar", avatar, "email", email);
    }
  }

  @Serdeable
  public static record UnlinkKickAccount(
    String recipientId,
    String refreshTokenId
  ) {}

  @Serdeable
  public static record UnsubscribeKickEventsCommand(
    String recipientId,
    String refreshTokenId
  ) {}
}
