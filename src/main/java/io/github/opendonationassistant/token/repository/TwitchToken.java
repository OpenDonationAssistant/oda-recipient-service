package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.twitch.TwitchClient;

public class TwitchToken extends RefreshToken {

  public TwitchToken(TwitchClient client, TokenData data, TokenDataRepository repository) {
    super(client, data, repository);
  }
}
