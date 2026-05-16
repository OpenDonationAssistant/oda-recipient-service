package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.kick.KickClient;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkKick extends BaseController {

  private final KickClient client;
  private final TokenRepository tokenRepository;
  private final RabbitClient rabbit;

  @Inject
  public LinkKick(
    KickClient client,
    TokenRepository tokenRepository,
    RabbitClient rabbit
  ) {
    this.client = client;
    this.tokenRepository = tokenRepository;
    this.rabbit = rabbit;
  }

  @Post("/recipients/commands/link-kick")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> linkKick(
    Authentication auth,
    @Body LinkKickCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return client
      .link(command.authorizationCode(), command.codeVerifier())
      .thenCompose(response ->
        client
          .getUser(response.accessToken())
          .thenApply(user -> {
            record UserData(
              Optional<KickClient.KickUser> user,
              String accessToken,
              String refreshToken
            ) {}
            return new UserData(
              user,
              response.accessToken(),
              response.refreshToken()
            );
          })
      )
      .thenApply(response -> {
        if (response.user().isEmpty()) {
          return HttpResponse.unauthorized();
        }
        var user = response.user().get();
        var token = tokenRepository.create(
          response.refreshToken(),
          "refreshToken",
          owner.get(),
          "Kick",
          Map.of(
            "id",
            user.id(),
            "name",
            user.name(),
            "email",
            user.email(),
            "avatar",
            user.avatar()
          )
        );
        token.save();
        rabbit.sendCommand(
          new SubscribeAllKickEventsCommand(
            owner.get(),
            response.accessToken(),
            token.data().id()
          )
        );
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record LinkKickCommand(
    String authorizationCode,
    String codeVerifier
  ) {}

  @Serdeable
  public static record SubscribeAllKickEventsCommand(
    String recipientId,
    String token,
    String refreshTokenId
  ) {}
}
