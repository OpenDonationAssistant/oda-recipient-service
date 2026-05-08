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
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkDiscord extends BaseController {

  private final TokenRepository tokenRepository;
  private final DiscordClient client;
  private final String redirect;
  private final String clientSecret;
  private final String clientId;

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
    this.clientId = clientId;
    this.clientSecret = clientSecret;
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
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    return client
      .getToken(params)
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
