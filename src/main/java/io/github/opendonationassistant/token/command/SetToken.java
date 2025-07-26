package io.github.opendonationassistant.token.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.Optional;

@Controller
public class SetToken extends BaseController {

  private TokenDataRepository repository;

  @Inject
  public SetToken(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/settoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<Void> setToken(
    Authentication auth,
    @Body SetTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    Optional.ofNullable(command.id())
      .map(repository::findById)
      .ifPresentOrElse(
        existed -> {
          repository.update(
            new TokenData(
              command.id(),
              command.token(),
              command.type(),
              owner.get(),
              command.system(),
              true
            )
          );
        },
        () -> {
          repository.save(
            new TokenData(
              Optional.ofNullable(command.id()).orElseGet(() ->
                Generators.timeBasedEpochGenerator().generate().toString()
              ),
              command.token(),
              command.type(),
              owner.get(),
              command.system(),
              true
            )
          );
        }
      );
    return HttpResponse.ok();
  }

  @Serdeable
  public static record SetTokenCommand(
    String id,
    String token,
    String type,
    String system
  ) {}
}
