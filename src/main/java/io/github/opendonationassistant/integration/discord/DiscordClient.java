package io.github.opendonationassistant.integration.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Client("vklive")
public interface DiscordClient {
  @Post(
    value = "/api/oauth2/token",
    produces = MediaType.APPLICATION_FORM_URLENCODED,
    consumes = MediaType.APPLICATION_FORM_URLENCODED
  )
  CompletableFuture<GetAccessRecordResponse> getToken(
    @Header("Authorization") String auth,
    @Body Map<String, String> params
  );

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
