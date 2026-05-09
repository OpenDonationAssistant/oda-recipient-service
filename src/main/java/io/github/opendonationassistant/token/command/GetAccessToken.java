package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.repository.RefreshToken;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
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
public class GetAccessToken extends BaseController {

  private final TokenRepository repository;

  @Inject
  public GetAccessToken(TokenRepository repository) {
    this.repository = repository;
  }

  @Post("/recipients/tokens/get-access-token")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @ApiResponse(
    description = "Fresh Access Token",
    responseCode = "200",
    content = @io.swagger.v3.oas.annotations.media.Content(
      mediaType = "application/json",
      schema = @io.swagger.v3.oas.annotations.media.Schema(
        implementation = GetAccessToken.AccessToken.class
      )
    )
  )
  @ApiResponse(responseCode = "401", description = "Unauthorized or not found")
  public CompletableFuture<HttpResponse<AccessToken>> getAccessToken(
    Authentication auth,
    @Body GetAccessTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return repository
      .findById(command.refreshTokenId())
      .filter(token -> token.data().recipientId().equals(owner.get()))
      .filter(token -> token instanceof RefreshToken)
      .map(token -> (RefreshToken) token)
      .map(token ->
        token
          .obtainAccessToken()
          .thenApply(it ->
            (HttpResponse<AccessToken>) HttpResponse.ok(new AccessToken(it))
          )
      )
      .orElseGet(() ->
        CompletableFuture.completedFuture(HttpResponse.unauthorized())
      );
  }

  @Serdeable
  public record AccessToken(String token) {}

  @Serdeable
  public static record GetAccessTokenCommand(String refreshTokenId) {}
}
