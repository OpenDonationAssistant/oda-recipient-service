package io.github.stcarolas.oda.gateway;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.problem.HttpStatusType;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.zalando.problem.Problem;

@Controller
public class GatewayController {

  private final GatewayRepository gatewayRepository;

  @Inject
  public GatewayController(GatewayRepository gatewayRepository) {
    this.gatewayRepository = gatewayRepository;
  }

  @Get("/gateways/{gateway}")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public GatewayConfigRestView getConfig(
    @PathVariable String gateway,
    Authentication auth
  ) {
    return gatewayRepository
      .find(getNickname(auth), gateway)
      .map(config -> new GatewayConfigRestView(config.getConfig()))
      .orElseThrow(() ->
        Problem
          .builder()
          .withStatus(new HttpStatusType(HttpStatus.NOT_FOUND))
          .build()
      );
  }

  private String getNickname(Authentication auth) {
    return String.valueOf(
      auth.getAttributes().getOrDefault("preferred_username", "")
    );
  }
}
