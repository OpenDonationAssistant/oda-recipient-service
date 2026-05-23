package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
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
import jakarta.inject.Named;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkTwitch extends BaseController {

  private final TwitchClient twitch;
  private final TokenRepository repository;
  private final RabbitClient rabbit;

  @Inject
  public LinkTwitch(
    TwitchClient twitch,
    TokenRepository repository,
    @Named("commands") RabbitClient rabbit
  ) {
    this.twitch = twitch;
    this.repository = repository;
    this.rabbit = rabbit;
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
              Optional<TwitchClient.TwitchUser> user,
              String refreshToken
            ) {}
            return new UserData(it, response.refreshToken());
          })
      )
      .thenApply(response -> {
        if (response.user().isEmpty()) {
          return HttpResponse.unauthorized();
        }
        var user = response.user().get();
        var token = repository.create(
          response.refreshToken(),
          "refreshToken",
          owner.get(),
          "Twitch",
          Map.of(
            "id",
            user.id(),
            "name",
            user.displayName(),
            "email",
            user.email(),
            "avatar",
            user.profileImageUrl()
          )
        );
        rabbit.sendCommand(
          new LinkTwitchAccount(
            owner.get(),
            user.id(),
            user.login(),
            token.data().id()
          )
        );
        rabbit.sendCommand(
          new SubscribeAllTwitchEventsCommand(owner.get(), token.data().id())
        );
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record SubscribeAllTwitchEventsCommand(
    String recipientId,
    String refreshTokenId
  ) {}

  @Serdeable
  public static record LinkTwitchAccount(
    String recipientId,
    String twitchId,
    String twitchLogin,
    String refreshTokenId
  ) {}

  @Serdeable
  public static record GetTwitchTokenCommand(String authorizationCode) {}
}
