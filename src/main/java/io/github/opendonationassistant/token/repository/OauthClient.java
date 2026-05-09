package io.github.opendonationassistant.token.repository;

import java.util.concurrent.CompletableFuture;

public interface OauthClient {
  CompletableFuture<String> obtainAccessToken(String refreshToken);
}
