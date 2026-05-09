package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.vklive.VKLiveClient;

public class VkliveToken extends RefreshToken {

  public VkliveToken(
    VKLiveClient client,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(client, data, repository);
  }
}
