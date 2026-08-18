package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.repository.OauthClient.RefreshedTokens;
import io.github.opendonationassistant.token.repository.RefreshToken;
import io.github.opendonationassistant.token.repository.Token;
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
public class RefreshTokens extends BaseController {

  private final TokenRepository repository;

  @Inject
  public RefreshTokens(TokenRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/refresh")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> refresh(
    Authentication auth,
    @Body RefreshTokensCommand command
  ) {
    if (!isAdmin(auth)) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.allOf(
      repository
        .findBySystem(command.system())
        .stream()
        .map(this::refresh)
        .toArray(CompletableFuture[]::new)
    ).thenApply(it -> HttpResponse.ok());
  }

  private CompletableFuture<RefreshedTokens> refresh(Token token) {
    if (token instanceof RefreshToken refreshToken) {
      return refreshToken.obtainAccessToken();
    }
    return CompletableFuture.completedFuture(null);
  }

  @Serdeable
  public static record RefreshTokensCommand(String system) {}
}
