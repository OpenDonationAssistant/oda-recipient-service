package io.github.opendonationassistant.integration.vklive;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.annotation.JsonProperty;

@Client("vklive")
public interface VKLive {
  @Post(
    value = "/oauth/server/token",
    consumes = MediaType.APPLICATION_FORM_URLENCODED
  )
  CompletableFuture<GetAccessRecordResponse> getToken(
    @Header("Authorization") String auth,
    @Body Map<String, String> params
  );

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken
  ) {}
}
