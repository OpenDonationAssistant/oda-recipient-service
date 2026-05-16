package io.github.opendonationassistant.integration.kick;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class KickClient implements OauthClient {

  private final KickClientAuthApi auth;
  private final KickClientDataApi data;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public KickClient(
    KickClientAuthApi auth,
    KickClientDataApi data,
    @Value("${kick-auth.redirect}") String redirect,
    @Value("${kick-auth.client.id}") String clientId,
    @Value("${kick-auth.client.secret}") String clientSecret
  ) {
    this.auth = auth;
    this.data = data;
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
    return auth.getToken(params);
  }

  public CompletableFuture<Optional<KickUser>> getUser(String accessToken) {
    return data
      .getUser("Bearer " + accessToken)
      .thenApply(response -> response.data().stream().findFirst());
  }

  @Client("kick-auth")
  public interface KickClientAuthApi {
    @Post(
      value = "/oauth/token",
      consumes = "application/json",
      produces = "application/x-www-form-urlencoded"
    )
    public CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Client("kick-data")
  public interface KickClientDataApi {
    @Get("/public/v1/users")
    public CompletableFuture<DataWrapper<KickUser>> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record DataWrapper<T>(List<T> data) {}

  @Serdeable
  public static record KickUser(
    String name,
    String email,
    @JsonProperty("profile_picture") String avatar,
    @JsonProperty("user_id") String id
  ) {}

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
    return auth
      .getToken(params)
      .thenApply(GetAccessRecordResponse::accessToken);
  }
}
