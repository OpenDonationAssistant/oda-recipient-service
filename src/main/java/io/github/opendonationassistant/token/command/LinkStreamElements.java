package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.streamelements.StreamElementsClient;
import io.github.opendonationassistant.integration.streamelements.StreamElementsClient.StreamElementsUser;
import io.github.opendonationassistant.token.repository.StreamElementsToken;
import io.github.opendonationassistant.token.repository.StreamElementsTokenRepository;
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
public class LinkStreamElements extends BaseController {

  private final StreamElementsClient streamelements;
  private final StreamElementsTokenRepository repository;

  @Inject
  public LinkStreamElements(
    StreamElementsClient streamelements,
    StreamElementsTokenRepository repository
  ) {
    this.streamelements = streamelements;
    this.repository = repository;
  }

  @Post("/recipients/commands/link-streamelements")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkStreamElements(
    Authentication auth,
    @Body GetStreamElementsTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return streamelements
      .link(command.authorizationCode())
      .thenCompose(response ->
        streamelements
          .getUser(response.accessToken())
          .thenApply(user -> {
            record UserData(StreamElementsUser account, String refreshToken) {}
            return new UserData(user, response.refreshToken());
          })
      )
      .thenCompose(response -> {
        var user = response.account();
        return repository
          .create(
            response.refreshToken(),
            owner.get(),
            new StreamElementsToken.Settings(
              user.id(),
              user.username(),
              user.displayName(),
              user.avatar()
            )
          )
          .thenApply(token -> HttpResponse.ok());
      });
  }

  @Serdeable
  public static record GetStreamElementsTokenCommand(String authorizationCode) {}
}
