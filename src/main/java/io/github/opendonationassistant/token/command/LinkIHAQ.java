package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.ihaq.IHAQClient;
import io.github.opendonationassistant.integration.ihaq.IHAQClient.IHAQUser;
import io.github.opendonationassistant.token.repository.IHAQToken;
import io.github.opendonationassistant.token.repository.IHAQTokenRepository;
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
public class LinkIHAQ extends BaseController {

  private final IHAQClient client;
  private final IHAQTokenRepository repository;

  @Inject
  public LinkIHAQ(IHAQClient client, IHAQTokenRepository repository) {
    this.client = client;
    this.repository = repository;
  }

  @Post("/recipients/commands/link-ihaq")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkIHAQ(
    Authentication auth,
    @Body GetIHAQTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return client
      .link(command.authorizationCode())
      .thenCompose(response ->
        client
          .getUser(response.accessToken())
          .thenApply(user -> {
            record UserData(IHAQUser user, String refreshToken) {}
            return new UserData(user, response.refreshToken());
          })
      )
      .thenCompose(response -> {
        var user = response.user();
        return repository
          .create(
            response.refreshToken(),
            owner.get(),
            new IHAQToken.Settings(
              user.id(),
              user.username(),
              user.apiToken()
            )
          )
          .thenApply(token -> HttpResponse.ok());
      });
  }

  @Serdeable
  public static record GetIHAQTokenCommand(String authorizationCode) {}
}