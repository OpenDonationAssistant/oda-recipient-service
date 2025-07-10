package io.github.opendonationassistant.info;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class InfoController {

  @Inject
  public InfoController() {}

  @Get("/session")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<Info> test(@Nullable Authentication auth) {
    if (auth == null) {
      return HttpResponse.ok(new Info(false, ""));
    }
    return HttpResponse.ok(
      new Info(
        true,
        String.valueOf(
          auth.getAttributes().getOrDefault("preferred_username", "")
        )
      )
    );
  }
}
