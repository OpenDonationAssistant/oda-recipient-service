package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.kick.KickClient;
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
public class LinkKick extends BaseController {

  private final KickClient client;
  private final TokenRepository tokenRepository;
  private final String redirect;
  private final String clientSecret;
  private final String clientId;

  @Inject
  public LinkKick(
    KickClient client,
    @Value("${kick.redirect}") String redirect,
    @Value("${kick.client.id}") String clientId,
    @Value("${kick.client.secret}") String clientSecret,
    TokenRepository tokenRepository
  ) {
    this.client = client;
    this.tokenRepository = tokenRepository;
    this.redirect = redirect;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Post("/recipients/commands/link-kick")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkDiscord(
    Authentication auth,
    @Body LinkKickCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    var params = new HashMap<String, String>();
    params.put("grant_type", "authorization_code");
    params.put("client_id", this.clientId);
    params.put("client_secret", this.clientSecret);
    params.put("redirect_uri", redirect);
    params.put("code_verifier", command.codeVerifier());
    params.put("code", command.authorizationCode());
    return client
      .getToken(params)
      .thenApply(response -> {
        tokenRepository
          .create(response.refreshToken(), "refreshToken", owner.get(), "Kick")
          .save();
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record LinkKickCommand(
    String authorizationCode,
    String codeVerifier
  ) {}
}
