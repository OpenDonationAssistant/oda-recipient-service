package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.kick.KickClient;

public class KickToken extends RefreshToken {

  public KickToken(
    KickClient oauth,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(oauth, data, repository);
  }
}
