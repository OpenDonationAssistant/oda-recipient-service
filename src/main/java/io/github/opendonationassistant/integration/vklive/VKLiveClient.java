package io.github.opendonationassistant.integration.vklive;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opendonationassistant.token.repository.OauthClient;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class VKLiveClient implements OauthClient {

  private final VKLiveClientAuthApi auth;
  private final VKLiveClientDataApi data;
  private final String redirect;
  private final String credentials;

  @Inject
  public VKLiveClient(
    VKLiveClientAuthApi auth,
    VKLiveClientDataApi data,
    @Value("${vklive-auth.redirect}") String redirect,
    @Value("${vklive-auth.client.id}") String clientId,
    @Value("${vklive-auth.client.secret}") String clientSecret
  ) {
    this.auth = auth;
    this.data = data;
    this.redirect = redirect;
    this.credentials =
      "Basic " +
      Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes());
  }

  public CompletableFuture<GetAccessRecordResponse> link(
    String authorizationCode
  ) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "authorization_code");
    params.put("code", authorizationCode);
    params.put("redirect_uri", redirect);
    return auth.getToken(credentials, params);
  }

  @Override
  public CompletableFuture<String> obtainAccessToken(String refreshToken) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", refreshToken);
    params.put("redirect_uri", redirect);
    return auth
      .getToken(credentials, params)
      .thenApply(response -> response.accessToken());
  }

  public CompletableFuture<VKLiveUser> getUser(String accessToken) {
    return data
      .getUser(accessToken)
      .thenApply(DataWrapper::data)
      .thenApply(UserWrapper::user);
  }

  @Client("vklive-auth")
  public interface VKLiveClientAuthApi {
    @Post(
      value = "/oauth/server/token",
      produces = MediaType.APPLICATION_FORM_URLENCODED,
      consumes = MediaType.APPLICATION_FORM_URLENCODED
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Header("Authorization") String auth,
      @Body Map<String, String> params
    );
  }

  @Client("vklive-data")
  public interface VKLiveClientDataApi {
    @Get("/v1/current_user")
    CompletableFuture<DataWrapper<UserWrapper>> getUser(
      @Header("Authorization") String auth
    );
  }

  @Serdeable
  public static record UserWrapper(VKLiveUser user) {}

  @Serdeable
  public static record VKLiveUser(
    String id,
    String nick,
    @JsonProperty("avatar_url") String avatarUrl
  ) {}

  @Serdeable
  public static record DataWrapper<T>(T data) {}

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
