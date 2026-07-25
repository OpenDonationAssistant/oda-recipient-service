package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.goodgame.GoodGameClient;
import io.github.opendonationassistant.integration.goodgame.GoodGameClient.GoodGameUser;
import io.github.opendonationassistant.token.repository.GoodGameToken;
import io.github.opendonationassistant.token.repository.GoodGameTokenRepository;
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
public class LinkGoodGame extends BaseController {

  private final GoodGameClient goodgame;
  private final GoodGameTokenRepository repository;

  @Inject
  public LinkGoodGame(
    GoodGameClient goodgame,
    GoodGameTokenRepository repository
  ) {
    this.goodgame = goodgame;
    this.repository = repository;
  }

  @Post("/recipients/commands/link-goodgame")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkGoodGame(
    Authentication auth,
    @Body GetGoodGameTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return goodgame
      .link(command.authorizationCode())
      .thenCompose(response ->
        goodgame
          .getUser(response.accessToken())
          .thenApply(user -> {
            record UserData(GoodGameUser account, String refreshToken) {}
            return new UserData(user, response.refreshToken());
          })
      )
      .thenCompose(response -> {
        var user = response.account();
        return repository
          .create(
            response.refreshToken(),
            owner.get(),
            new GoodGameToken.Settings(
              String.valueOf(user.id()),
              user.nickname(),
              user.avatar()
            )
          )
          .thenApply(token -> HttpResponse.ok());
      });
  }

  @Serdeable
  public static record GetGoodGameTokenCommand(String authorizationCode) {}
}
