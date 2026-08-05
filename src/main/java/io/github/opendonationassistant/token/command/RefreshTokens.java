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
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class RefreshTokens extends BaseController {

  private final TokenRepository repository;

  @Inject
  public RefreshTokens(TokenRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/refresh")
  public CompletableFuture<HttpResponse<List<RefreshedTokens>>> refresh(
    @Body RefreshTokensCommand command
  ) {
    return CompletableFuture.supplyAsync(() -> {
      var refreshed = repository
        .findBySystem(command.system())
        .stream()
        .map(this::refresh)
        .toList();
      return HttpResponse.ok(refreshed);
    });
  }

  private RefreshedTokens refresh(Token token) {
    if (token instanceof RefreshToken refreshToken) {
      return refreshToken.obtainAccessToken().join();
    }
    return new RefreshedTokens(token.data().token(), token.data().token());
  }

  @Serdeable
  public static record RefreshTokensCommand(String system) {}
}