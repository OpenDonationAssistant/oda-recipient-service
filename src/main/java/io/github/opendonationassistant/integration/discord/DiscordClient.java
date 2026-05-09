package io.github.opendonationassistant.integration.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opendonationassistant.token.repository.OauthClient;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
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
public class DiscordClient implements OauthClient {

  private final DiscordClientApi client;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public DiscordClient(
    DiscordClientApi client,
    @Value("${discord.redirect}") String redirect,
    @Value("${discord.client.id}") String clientId,
    @Value("${discord.client.secret}") String clientSecret
  ) {
    this.client = client;
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
    return client.getToken(params);
  }

  @Override
  public CompletableFuture<String> obtainAccessToken(String refreshToken) {
    var params = new HashMap<String, String>();
    params.put("grant_type", "refresh_token");
    params.put("refresh_token", refreshToken);
    params.put("redirect_uri", redirect);
    return client
      .getToken(params)
      .thenApply(response -> response.accessToken());
  }

  @Client("discord")
  public interface DiscordClientApi {
    @Post(
      value = "/api/oauth2/token",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_FORM_URLENCODED
    )
    CompletableFuture<GetAccessRecordResponse> getToken(
      @Body Map<String, String> params
    );
  }

  @Serdeable
  public static record GetAccessRecordResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
  ) {}
}
