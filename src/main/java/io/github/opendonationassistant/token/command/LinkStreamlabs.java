package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.streamlabs.StreamlabsClient;
import io.github.opendonationassistant.integration.streamlabs.StreamlabsClient.StreamlabsAccount;
import io.github.opendonationassistant.token.repository.StreamlabsToken;
import io.github.opendonationassistant.token.repository.StreamlabsTokenRepository;
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
public class LinkStreamlabs extends BaseController {

  private final StreamlabsClient streamlabs;
  private final StreamlabsTokenRepository repository;

  @Inject
  public LinkStreamlabs(
    StreamlabsClient streamlabs,
    StreamlabsTokenRepository repository
  ) {
    this.streamlabs = streamlabs;
    this.repository = repository;
  }

  @Post("/recipients/commands/link-streamlabs")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkStreamlabs(
    Authentication auth,
    @Body GetStreamlabsTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return streamlabs
      .link(command.authorizationCode())
      .thenCompose(response ->
        streamlabs
          .getUser(response.accessToken())
          .thenApply(user -> {
            record UserData(
              StreamlabsAccount account,
              String refreshToken
            ) {}
            return new UserData(user.streamlabs(), response.refreshToken());
          })
      )
      .thenCompose(response -> {
        var user = response.account();
        return repository
          .create(
            response.refreshToken(),
            owner.get(),
            new StreamlabsToken.Settings(
              String.valueOf(user.id()),
              user.displayName()
            )
          )
          .thenApply(token -> HttpResponse.ok());
      });
  }

  @Serdeable
  public static record GetStreamlabsTokenCommand(String authorizationCode) {}
}
