package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.vklive.VKLiveClient;
import io.github.opendonationassistant.rabbit.RabbitClient;

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
}
