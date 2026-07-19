package io.github.opendonationassistant.integration.streamelements;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opendonationassistant.token.repository.OauthClient;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class StreamElementsClient implements OauthClient {

  private final StreamElementsClientAuthApi auth;
  private final StreamElementsClientDataApi data;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public StreamElementsClient(
    StreamElementsClientAuthApi auth,
    StreamElementsClientDataApi data,
    @Value("${streamelements-auth.redirect}") String redirect,
    @Value("${streamelements-auth.client.id}") String clientId,
    @Value("${streamelements-auth.client.secret}") String clientSecret
  ) {
    this.auth = auth;
    this.data = data;
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
    return auth.getToken(params);
  }

  @Override
  public CompletableFuture<RefreshedTokens> obtainAccessToken(
    String refreshToken
  ) {
    var params = new HashMap<String, String>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", refreshToken);
    return auth
      .getToken(params)
      .thenApply(response ->
        new RefreshedTokens(response.accessToken(), response.refreshToken())
      );
  }

  public CompletableFuture<StreamElementsUser> getUser(String accessToken) {
    return data.getUser("oAuth " + accessToken);
  }

  @Client("streamelements-auth")
  public static interface StreamElementsClientAuthApi {
    @Post(
      value = "/oauth2/token",
      consumes = "application/json",
      produces = "application/x-www-form-urlencoded"
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Client("streamelements-data")
  public static interface StreamElementsClientDataApi {
    @Get("/kappa/v2/channels/me")
    CompletableFuture<StreamElementsUser> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record StreamElementsUser(
    @JsonProperty("_id") String id,
    String username,
    @JsonProperty("displayName") String displayName,
    String email,
    String avatar,
    String alias,
    String provider,
    @JsonProperty("providerId") String providerId,
    @JsonProperty("broadcasterType") String broadcasterType,
    String type,
    boolean suspended
  ) {}

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
