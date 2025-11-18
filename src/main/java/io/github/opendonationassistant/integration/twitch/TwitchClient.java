package io.github.opendonationassistant.integration.twitch;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
