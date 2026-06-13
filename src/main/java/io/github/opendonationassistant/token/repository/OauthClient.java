package io.github.opendonationassistant.token.repository;

import io.micronaut.serde.annotation.Serdeable;
import java.util.concurrent.CompletableFuture;

public interface OauthClient {
  CompletableFuture<RefreshedTokens> obtainAccessToken(String refreshToken);

  @Serdeable
  public static record RefreshedTokens(
    String accessToken,
    String refreshToken
  ) {}
}
