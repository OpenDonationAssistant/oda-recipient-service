package io.github.opendonationassistant.integration.twitch;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Client("twitch")
public interface TwitchClient {
  @Post(
    value = "/oauth2/token",
    produces = "application/json",
    consumes = "application/x-www-form-urlencoded"
  )
  public CompletableFuture<GetAccessRecordResponse> getToken(
    @Body Map<String, String> request
  );

  @Serdeable
  public static record GetAccessRecordResponse(
    String accessToken,
    String refreshToken
  ) {}
}
