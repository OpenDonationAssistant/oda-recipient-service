package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.discord.DiscordClient;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkDiscord extends BaseController {

  private final TokenRepository tokenRepository;
  private final DiscordClient client;
  private final String credentials;
  private final String redirect;

  @Inject
  public LinkDiscord(
    DiscordClient client,
    @Value("${discord.redirect}") String redirect,
    @Value("${discord.client.id}") String clientId,
    @Value("${discord.client.secret}") String clientSecret,
    TokenRepository tokenRepository
  ) {
    this.client = client;
    this.tokenRepository = tokenRepository;
    this.redirect = redirect;
    this.credentials =
      "Basic " +
      Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes());
  }

  @Post("/recipients/commands/link-discord")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkDiscord(
    Authentication auth,
    @Body LinkDiscordCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    var params = new HashMap<String, String>();
    params.put("grant_type", "authorization_code");
    params.put("code", command.authorizationCode());
    params.put("redirect_uri", redirect);
    return client
      .getToken(credentials, params)
      .thenApply(response -> {
        tokenRepository
          .create(
            response.refreshToken(),
            "refreshToken",
            owner.get(),
            "Discord"
          )
          .save();
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record LinkDiscordCommand(String authorizationCode) {}
}
