package io.github.opendonationassistant.token.command;

import java.util.concurrent.CompletableFuture;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.annotation.Serdeable;

@Controller
public class GetAccessToken extends BaseController {

  private final TwitchClient twitch;
  private final TokenRepository repository;

  @Post
  public CompletableFuture<HttpResponse<AccessToken>> getAccessToken(
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
      .map(token -> {
        var params = new HashMap<String, String>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", token.data().token());
        return params;
      })
      .map(params ->
        twitch
          .getToken(params)
          .thenApply(token ->
            (HttpResponse<AccessToken>) HttpResponse.ok(
              new AccessToken(token.accessToken())
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
