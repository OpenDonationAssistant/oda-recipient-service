package io.github.opendonationassistant.token.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.vklive.VKLive;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Controller
public class GetVKLiveToken extends BaseController {

  private final VKLive vklive;
  private final String redirect;
  private final String credentials;
  private final TokenRepository repository;

  @Inject
  public GetVKLiveToken(
    VKLive vklive,
    @Value("${vklive.redirect}") String redirect,
    @Value("${vklive.clientId}") String clientId,
    @Value("${vklive.clientSecret}") String clientSecret,
    TokenRepository repository
  ) {
    this.vklive = vklive;
    this.redirect = redirect;
    this.credentials = Base64.getEncoder()
      .encodeToString((clientId + ":" + clientSecret).getBytes());
    this.repository = repository;
  }

  @Post("/recipients/tokens/getvklivetoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> getVKLiveToken(
    Authentication auth,
    @Body GetVKLiveTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    var params = new HashMap<String, String>();
    params.put("grant_type", "authorization_code");
    params.put("code", command.authorizationCode());
    params.put("redirect_uri", redirect);
    return vklive
      .getToken(credentials, params)
      .thenApply(response -> {
        var token = repository.create(
          response.accessToken(),
          "accessToken",
          owner.get(),
          "VKLive"
        );
        token.save();
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record GetVKLiveTokenCommand(String authorizationCode) {}
}
