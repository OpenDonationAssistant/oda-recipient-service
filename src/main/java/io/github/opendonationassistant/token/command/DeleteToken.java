package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;

@Controller
public class DeleteToken extends BaseController {

  private final TokenDataRepository repository;

  public DeleteToken(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/deletetoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<Void> deleteToken(
    Authentication auth,
    @Body DeleteTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    repository.deleteById(command.id());
    return HttpResponse.ok();
  }

  @Serdeable
  public static record DeleteTokenCommand(String id) {}
}
