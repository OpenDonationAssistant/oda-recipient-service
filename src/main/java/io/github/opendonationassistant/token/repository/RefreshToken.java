package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.token.repository.OauthClient.RefreshedTokens;
import java.util.concurrent.CompletableFuture;

public class RefreshToken extends GenericToken {

  private final OauthClient oauth;

  public RefreshToken(
    OauthClient oauth,
    TokenData data,
    TokenDataRepository repository
  ) {
    super(data, repository);
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
