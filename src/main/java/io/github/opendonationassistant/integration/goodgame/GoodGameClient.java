package io.github.opendonationassistant.integration.goodgame;

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
public class GoodGameClient implements OauthClient {

  private final GoodGameClientAuthApi auth;
  private final GoodGameClientDataApi data;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public GoodGameClient(
    GoodGameClientAuthApi auth,
    GoodGameClientDataApi data,
    @Value("${goodgame-auth.redirect}") String redirect,
    @Value("${goodgame-auth.client.id}") String clientId,
    @Value("${goodgame-auth.client.secret}") String clientSecret
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

  public CompletableFuture<GoodGameUser> getUser(String accessToken) {
    return data.getUser("Bearer " + accessToken);
  }

  @Client("goodgame-auth")
  public static interface GoodGameClientAuthApi {
    @Post(
      value = "/oauth2/token",
      consumes = "application/json",
      produces = "application/x-www-form-urlencoded"
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Client("goodgame-data")
  public static interface GoodGameClientDataApi {
    @Get("/api/4/user/")
    CompletableFuture<GoodGameUser> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record GoodGameUser(
    int id,
    String nickname,
    String username,
    String avatar,
    String email
  ) {}

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
