package io.github.opendonationassistant.integration.ihaq;

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
public class IHAQClient implements OauthClient {

  private final IHAQClientAuthApi auth;
  private final IHAQClientDataApi data;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public IHAQClient(
    IHAQClientAuthApi auth,
    IHAQClientDataApi data,
    @Value("${ihaq.redirect}") String redirect,
    @Value("${ihaq.client.id}") String clientId,
    @Value("${ihaq.client.secret}") String clientSecret
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
    params.put("redirect_uri", redirect);
    return auth
      .getToken(params)
      .thenApply(response ->
        new RefreshedTokens(response.accessToken(), response.refreshToken())
      );
  }

  public CompletableFuture<IHAQUser> getUser(String accessToken) {
    return data.getUser("Bearer " + accessToken);
  }

  @Client("ihaq")
  public static interface IHAQClientAuthApi {
    @Post(
      value = "/api/v2/oauth/token",
      consumes = "application/json",
      produces = "application/json"
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Client("ihaq")
  public static interface IHAQClientDataApi {
    @Get("/api/v2/user")
    CompletableFuture<IHAQUser> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record IHAQUser(
    String id,
    String username,
    String email,
    @JsonProperty("api_token") String apiToken
  ) {}

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") int expiresIn,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
