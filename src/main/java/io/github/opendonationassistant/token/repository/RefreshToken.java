package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.repository.OauthClient.RefreshedTokens;
import java.util.concurrent.CompletableFuture;

public class RefreshToken extends GenericToken {

  private final OauthClient oauth;

  public RefreshToken(
    OauthClient oauth,
    TokenData data,
    TokenDataRepository repository,
    RabbitClient events
  ) {
    super(data, repository, events);
    this.oauth = oauth;
  }

  public CompletableFuture<RefreshedTokens> obtainAccessToken() {
    return oauth
      .obtainAccessToken(this.data().token())
      .thenApply(response -> {
        this.update(response.refreshToken());
        return response;
      });
  }
}
