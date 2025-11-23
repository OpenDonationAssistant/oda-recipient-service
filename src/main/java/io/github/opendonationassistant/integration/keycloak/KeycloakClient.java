package io.github.opendonationassistant.integration.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.keycloak.representations.idm.UserRepresentation;

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
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken
  ) {}

  @Put("/realms/ODA/users/{user-id}")
  CompletableFuture<Void> updateUser(
    @PathVariable String userId,
    @Body UserRepresentation user
  );

  @Put("/realms/ODA/users/{user-id}/send-verify-email")
  CompletableFuture<Void> sendVerifyEmail(@PathVariable String userId, @Body Map<String, String> params);
}
