package io.github.stcarolas.oda.gateway;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

@Controller
public class GatewayCommandController {

  @Put("/commands/gateway")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public void put(UpdateGatewayConfigCommand command, Authentication auth) {
    command.execute(getNickname(auth));
  }

  private String getNickname(Authentication auth) {
    return String.valueOf(
      auth.getAttributes().getOrDefault("preferred_username", "")
    );
  }
}
