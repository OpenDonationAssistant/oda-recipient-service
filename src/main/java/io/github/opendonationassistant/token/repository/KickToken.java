package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.kick.KickClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
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
          new UnsubscribeKickEventsCommand(
            data().recipientId(),
            token,
            data().id()
          )
        );
        return super.delete();
      });
  }

  @Serdeable
  public static record UnsubscribeKickEventsCommand(
    String recipientId,
    String token,
    String refreshTokenId
  ) {}
}
