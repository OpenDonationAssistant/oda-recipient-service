package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.vklive.VKLiveClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class VkliveToken extends RefreshToken {

  private final RabbitClient rabbit;

  public VkliveToken(
    VKLiveClient client,
    TokenData data,
    TokenDataRepository repository,
    RabbitClient rabbit
  ) {
    super(client, data, repository);
    this.rabbit = rabbit;
  }

  @Override
  public CompletableFuture<Void> delete() {
    rabbit.sendCommand(new UnlinkVkAccount(data().recipientId(), data().id()));
    return super.delete();
  }

  @Serdeable
  public static record UnlinkVkAccount(
    String recipientId,
    String refreshTokenId
  ) {}
}
