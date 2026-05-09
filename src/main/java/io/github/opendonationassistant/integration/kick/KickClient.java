package io.github.opendonationassistant.integration.kick;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opendonationassistant.token.repository.OauthClient;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class KickClient implements OauthClient {

  private final KickClientApi client;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public KickClient(
    KickClientApi client,
    @Value("${kick.redirect}") String redirect,
    @Value("${kick.client.id}") String clientId,
    @Value("${kick.client.secret}") String clientSecret
  ) {
    this.client = client;
    this.redirect = redirect;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public CompletableFuture<GetAccessRecordResponse> link(
    String authorizationCode,
    String codeVerifier
  ) {
    var params = new HashMap<String, String>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("grant_type", "authorization_code");
    params.put("code", authorizationCode);
    params.put("code_verifier", codeVerifier);
    params.put("redirect_uri", redirect);
    return client.getToken(params);
  }

  @Client("kick")
  public interface KickClientApi {
    @Post(
      value = "/oauth/token",
      consumes = "application/json",
      produces = "application/x-www-form-urlencoded"
    )
    public CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}

  @Override
  public CompletableFuture<String> obtainAccessToken(String token) {
    var params = new HashMap<String, String>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", token);
    return client
      .getToken(params)
      .thenApply(response -> response.accessToken());
  }
}
