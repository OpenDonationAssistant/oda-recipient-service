package io.github.opendonationassistant.integration.twitch;

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
public class TwitchClient implements OauthClient {

  private final TwitchClientApi api;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public TwitchClient(
    TwitchClientApi api,
    @Value("${twitch.redirect}") String redirect,
    @Value("${twitch.client.id}") String clientId,
    @Value("${twitch.client.secret}") String clientSecret
  ) {
    this.api = api;
    this.redirect = redirect;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public CompletableFuture<GetAccessRecordResponse> link(
    String authorizationCode
  ) {
    var params = new HashMap<String, String>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("grant_type", "authorization_code");
    params.put("code", authorizationCode);
    params.put("redirect_uri", redirect);
    return api.getToken(params);
  }

  @Override
  public CompletableFuture<String> obtainAccessToken(String refreshToken) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", refreshToken);
    params.put("redirect_uri", redirect);
    return api.getToken(params).thenApply(response -> response.accessToken());
  }

  @Client("twitch")
  public static interface TwitchClientApi {
    @Post(
      value = "/oauth2/token",
      consumes = "application/json",
      produces = "application/x-www-form-urlencoded"
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
