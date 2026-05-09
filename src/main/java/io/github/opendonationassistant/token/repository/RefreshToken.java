package io.github.opendonationassistant.token.repository;

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

  public CompletableFuture<String> obtainAccessToken() {
    return oauth.obtainAccessToken(this.data().token());
  }
}
