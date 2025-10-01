package io.github.opendonationassistant.integration.keycloak;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.annotation.JsonProperty;

@Client("keycloak")
public interface KeycloakClient {
  @Post(
    value = "/realms/ODA/protocol/openid-connect/token",
    produces = MediaType.APPLICATION_FORM_URLENCODED,
    consumes = MediaType.APPLICATION_JSON
  )
  CompletableFuture<GetAccessRecordResponse> getAccessToken(
    @Body Map<String, String> params
  );

  @Serdeable
  public static record GetAccessRecordResponse(@JsonProperty("access_token") String accessToken) {}
}
