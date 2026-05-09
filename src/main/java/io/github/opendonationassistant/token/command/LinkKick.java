package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.kick.KickClient;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkKick extends BaseController {

  private final KickClient client;
  private final TokenRepository tokenRepository;

  @Inject
  public LinkKick(KickClient client, TokenRepository tokenRepository) {
    this.client = client;
    this.tokenRepository = tokenRepository;
  }

  @Post("/recipients/commands/link-kick")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkKick(
    Authentication auth,
    @Body LinkKickCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return client
      .link(command.authorizationCode(), command.codeVerifier())
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
