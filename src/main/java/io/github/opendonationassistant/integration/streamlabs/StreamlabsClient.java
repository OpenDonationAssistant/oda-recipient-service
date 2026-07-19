package io.github.opendonationassistant.integration.streamlabs;

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
public class StreamlabsClient implements OauthClient {

  private final StreamlabsClientAuthApi auth;
  private final StreamlabsClientDataApi data;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public StreamlabsClient(
    StreamlabsClientAuthApi auth,
    StreamlabsClientDataApi data,
    @Value("${streamlabs-auth.redirect}") String redirect,
    @Value("${streamlabs-auth.client.id}") String clientId,
    @Value("${streamlabs-auth.client.secret}") String clientSecret
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

  public CompletableFuture<StreamlabsUserResponse> getUser(String accessToken) {
    return data.getUser("Bearer " + accessToken);
  }

  @Client("streamlabs-auth")
  public static interface StreamlabsClientAuthApi {
    @Post(
      value = "/api/v2.0/token",
      consumes = "application/json",
      produces = "application/x-www-form-urlencoded"
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> request
    );
  }

  @Client("streamlabs-data")
  public static interface StreamlabsClientDataApi {
    @Get("/api/v2.0/user")
    CompletableFuture<StreamlabsUserResponse> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record StreamlabsUserResponse(
    StreamlabsAccount streamlabs
  ) {}

  @Serdeable
  public static record StreamlabsAccount(
    int id,
    @JsonProperty("display_name") String displayName,
    String name
  ) {}

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
