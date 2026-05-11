package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.vklive.VKLiveClient;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkVklive extends BaseController {

  private final VKLiveClient vklive;
  private final TokenRepository repository;

  @Inject
  public LinkVklive(VKLiveClient vklive, TokenRepository repository) {
    this.vklive = vklive;
    this.repository = repository;
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
        repository
          .create(
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
          )
          .save();
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record GetVKLiveTokenCommand(String authorizationCode) {}
}
