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
public class ToggleToken extends BaseController {

  private final TokenDataRepository repository;

  public ToggleToken(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/toggletoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<Void> toggleToken(
    Authentication auth,
    @Body ToggleTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    repository
      .findById(command.id())
      .map(data -> data.withEnabled(command.enabled()))
      .ifPresent(repository::update);
    return HttpResponse.ok();
  }

  @Serdeable
  public static record ToggleTokenCommand(String id, boolean enabled) {}
}
