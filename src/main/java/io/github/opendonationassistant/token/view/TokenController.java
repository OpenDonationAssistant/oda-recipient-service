package io.github.opendonationassistant.token.view;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Controller
public class TokenController extends BaseController {

  private final TokenDataRepository repository;

  public TokenController(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Get("/recipients/tokens")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<List<TokenDto>> listTokens(Authentication auth) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    return HttpResponse.ok(
      repository
        .findByRecipientId(owner.get())
        .stream()
        .map(this::convert)
        .toList()
    );
  }

  private TokenDto convert(TokenData data) {
    return new TokenDto(data.id(), data.system(), data.type(), data.token());
  }

  @Serdeable
  public static record TokenDto(
    String id,
    String system,
    String type,
    String token
  ) {}
}
