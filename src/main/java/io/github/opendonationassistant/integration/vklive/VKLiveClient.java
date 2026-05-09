package io.github.opendonationassistant.integration.vklive;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opendonationassistant.token.repository.OauthClient;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
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

  private final VKLiveClientApi vklive;
  private final String redirect;
  private final String credentials;

  @Inject
  public VKLiveClient(
    VKLiveClientApi vklive,
    @Value("${vklive.redirect}") String redirect,
    @Value("${vklive.client.id}") String clientId,
    @Value("${vklive.client.secret}") String clientSecret
  ) {
    this.vklive = vklive;
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
    return vklive.getToken(credentials, params);
  }

  @Override
  public CompletableFuture<String> obtainAccessToken(String refreshToken) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", refreshToken);
    params.put("redirect_uri", redirect);
    return vklive
      .getToken(credentials, params)
      .thenApply(response -> response.accessToken());
  }

  @Client("vklive")
  public interface VKLiveClientApi {
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

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
