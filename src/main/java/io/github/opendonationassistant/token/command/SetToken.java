package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.repository.Token;
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
public class SetToken extends BaseController {

  private TokenRepository repository;
  private ODALogger log = new ODALogger(this);

  @Inject
  public SetToken(TokenRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/set-token")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> setToken(
    Authentication auth,
    @Body SetTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    var existing = repository.findById(command.id());
    CompletableFuture<Token> token;
    if (existing.isEmpty()) {
      log.debug("Existing token not found", Map.of("id", command.id()));
      token = repository.create(
        command.id(),
        command.token(),
        command.type(),
        command.system(),
        command.settings()
      );
    } else {
      var existingToken = existing.get();
      if (!existingToken.data().recipientId().equals(owner.get())) {
        log.debug(
          "Existing token does not belong to user",
          Map.of(
            "id",
            command.id(),
            "recipientId",
            owner.get(),
            "ownerId",
            existingToken.data().recipientId()
          )
        );
        return CompletableFuture.completedFuture(HttpResponse.unauthorized());
      }
      log.debug(
        "Updating existing token",
        Map.of("id", command.id(), "recipientId", owner.get())
      );
      existingToken.update(command.settings());
      token = CompletableFuture.completedFuture(existingToken);
    }
    return token.thenApply(it -> HttpResponse.ok());
  }

  @Serdeable
  public static record SetTokenCommand(
    String id,
    String token,
    String type,
    String system,
    Map<String, Object> settings
  ) {}
}
