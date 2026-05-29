package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.vklive.VKLiveClient;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkVklive extends BaseController {

  private final VKLiveClient vklive;
  private final TokenRepository repository;
  private final RabbitClient rabbit;

  @Inject
  public LinkVklive(
    VKLiveClient vklive,
    TokenRepository repository,
    RabbitClient rabbit
  ) {
    this.vklive = vklive;
    this.repository = repository;
    this.rabbit = rabbit;
  }

  @Post("/recipients/commands/link-vklive")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkVKlive(
    Authentication auth,
    @Body GetVKLiveTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return vklive
      .link(command.authorizationCode())
      .thenCompose(response ->
        vklive
          .getUser(response.accessToken())
          .thenApply(it -> {
            record UserData(
              VKLiveClient.VKLiveUser user,
              String refreshToken
            ) {}
            return new UserData(it, response.refreshToken());
          })
      )
      .thenApply(response -> {
        var token = repository.create(
          response.refreshToken(),
          "refreshToken",
          owner.get(),
          "VKLive",
          Map.of(
            "id",
            response.user().id(),
            "name",
            response.user().nick(),
            "avatar",
            response.user().avatarUrl()
          )
        );
        rabbit.sendCommand(
          new LinkVkAccount(
            owner.get(),
            token.data().id(),
            response.user().id()
          )
        );
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record GetVKLiveTokenCommand(String authorizationCode) {}

  @Serdeable
  public static record LinkVkAccount(
    String recipientId,
    String refreshTokenId,
    String id
  ) {}
}
