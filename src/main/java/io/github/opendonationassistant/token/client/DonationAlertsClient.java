package io.github.opendonationassistant.token.client;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Client(id = "donationalerts")
public interface DonationAlertsClient {
  @Post(
    value = "/oauth/token",
    produces = MediaType.APPLICATION_FORM_URLENCODED
  )
  CompletableFuture<TokenResponse> getToken(@Body Map<String, String> request);

  @Serdeable
  public static record TokenResponse(
    String tokenType,
    String accessToken,
    String refreshToken
  ) {}
}
