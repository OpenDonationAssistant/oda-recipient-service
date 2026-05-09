package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.github.opendonationassistant.token.repository.TwitchToken;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Controller
public class GetTwitchAccessToken extends BaseController {

  private final TokenRepository repository;

  @Inject
  public GetTwitchAccessToken(
    TwitchClient twitch,
    TokenRepository repository,
    @Value("${twitch.client.id}") String clientId,
    @Value("${twitch.client.secret}") String clientSecret
  ) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/get-twitch-access-token")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @ApiResponse(
    description = "Twitch access token",
    responseCode = "200",
    content = @io.swagger.v3.oas.annotations.media.Content(
      mediaType = "application/json",
      schema = @io.swagger.v3.oas.annotations.media.Schema(
        implementation = GetTwitchAccessToken.AccessToken.class
      )
    )
  )
  @ApiResponse(responseCode = "401", description = "Unauthorized or not found")
  public CompletableFuture<HttpResponse<AccessToken>> getTwitchAccessToken(
    Authentication auth
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return repository
      .findByRecipientIdAndSystemAndType(owner.get(), "Twitch", "refreshToken")
      .stream()
      .findFirst()
      .map(token -> (TwitchToken) token)
      .map(token ->
        token
          .obtainAccessToken()
          .thenApply(accessToken ->
            (HttpResponse<AccessToken>) HttpResponse.ok(
              new AccessToken(accessToken)
            )
          )
      )
      .orElseGet(() ->
        CompletableFuture.completedFuture(HttpResponse.unauthorized())
      );
  }

  @Serdeable
  public record AccessToken(String token) {}
}
