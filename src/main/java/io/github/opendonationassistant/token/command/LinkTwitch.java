package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.twitch.TwitchClient;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkTwitch extends BaseController {

  private final TwitchClient twitch;
  private final TokenRepository repository;

  @Inject
  public LinkTwitch(TwitchClient twitch, TokenRepository repository) {
    this.twitch = twitch;
    this.repository = repository;
  }

  @Post("/recipients/commands/link-twitch")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkTwitch(
    Authentication auth,
    @Body GetTwitchTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return twitch
      .link(command.authorizationCode())
      .thenCompose(response ->
        twitch
          .getUser(response.accessToken())
          .thenApply(it -> {
            record UserData(
              TwitchClient.TwitchUser user,
              String refreshToken
            ) {}
            return new UserData(it, response.refreshToken());
          })
      )
      .thenApply(response -> {
        repository
          .create(
            response.refreshToken(),
            "refreshToken",
            owner.get(),
            "Twitch",
            Map.of(
              "id",
              response.user().id(),
              "name",
              response.user().displayName(),
              "email",
              response.user().email(),
              "avatar",
              response.user().profileImageUrl()
            )
          )
          .save();
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record GetTwitchTokenCommand(String authorizationCode) {}
}
