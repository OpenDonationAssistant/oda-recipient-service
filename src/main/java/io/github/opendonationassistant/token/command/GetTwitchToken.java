package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Controller
public class GetTwitchToken extends BaseController {

  private final TwitchClient twitch;
  private final TokenRepository repository;
  private final String redirect;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public GetTwitchToken(
    TwitchClient twitch,
    TokenRepository repository,
    @Value("${twitch.redirect}") String redirect,
    @Value("${twitch.client.id}") String clientId,
    @Value("${twitch.client.secret}") String clientSecret
  ) {
    this.twitch = twitch;
    this.repository = repository;
    this.redirect = redirect;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Post("/recipients/tokens/gettwitchtoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> getTwitchToken(
    Authentication auth,
    @Body GetTwitchTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    var params = new HashMap<String, String>();
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("grant_type", "authorization_code");
    params.put("code", command.authorizationCode());
    params.put("redirect_uri", redirect);
    return twitch
      .getToken(params)
      .thenApply(response -> {
        var token = repository.create(
          response.accessToken(),
          "accessToken",
          owner.get(),
          "Twitch"
        );
        token.save();
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record GetTwitchTokenCommand(String authorizationCode) {}
}
