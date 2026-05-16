package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.rabbit.RabbitClient;

public class TwitchToken extends RefreshToken {

  private RabbitClient rabbit;

  public TwitchToken(
    TwitchClient client,
    TokenData data,
    TokenDataRepository repository,
    RabbitClient rabbit
  ) {
    super(client, data, repository);
    this.rabbit = rabbit;
  }
}
