package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.discord.DiscordClient;

public class DiscordToken extends RefreshToken {

  public DiscordToken(
    DiscordClient client,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(client, data, repository);
  }
}
