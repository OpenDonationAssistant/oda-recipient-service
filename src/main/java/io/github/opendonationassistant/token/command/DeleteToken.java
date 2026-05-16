package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
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
public class DeleteToken extends BaseController {

  private final TokenRepository repository;

  @Inject
  public DeleteToken(TokenRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/deletetoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> deleteToken(
    Authentication auth,
    @Body DeleteTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return repository
      .findById(command.id())
      .map(token ->
        token
          .delete()
          .thenApply(_ -> (HttpResponse<Void>) HttpResponse.<Void>ok())
      )
      .orElseGet(() ->
        CompletableFuture.completedFuture(HttpResponse.notFound())
      );
  }

  @Serdeable
  public static record DeleteTokenCommand(String id) {}
}
