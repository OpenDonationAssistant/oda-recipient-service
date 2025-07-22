package io.github.opendonationassistant.token.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.token.client.DonationAlertsClient;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
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
import java.util.HashMap;

@Controller
public class GetDonationAlertsToken extends BaseController {

  private final DonationAlertsClient client;
  private final String clientId;
  private final String clientSecret;
  private final TokenDataRepository repository;

  @Inject
  public GetDonationAlertsToken(
    DonationAlertsClient client,
    TokenDataRepository repository,
    @Value("${donationalerts.id}") String clientId,
    @Value("${donationalerts.secret}") String clientSecret
  ) {
    this.client = client;
    this.repository = repository;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Post("/recipients/tokens/getdonationalertstoken")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<Void> getDonationAlertsToken(
    Authentication auth,
    @Body GetDonationAlertsTokenCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    var params = new HashMap<String, String>();
    params.put("grant_type", "authorization_code");
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    params.put("code", command.authorizationCode());
    client
      .getToken(params)
      .thenAccept(response -> {
        repository.save(
          new TokenData(
            Generators.timeBasedEpochGenerator().generate().toString(),
            response.accessToken(),
            "accessToken",
            owner.get(),
            "DonationAlerts"
          )
        );
      });
    return HttpResponse.ok();
  }

  @Serdeable
  public static record GetDonationAlertsTokenCommand(
    String authorizationCode
  ) {}
}
