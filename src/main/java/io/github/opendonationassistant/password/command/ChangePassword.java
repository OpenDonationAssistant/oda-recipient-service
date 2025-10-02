package io.github.opendonationassistant.password.command;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.keycloak.KeycloakClient;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.zalando.problem.Problem;

@Controller
public class ChangePassword extends BaseController {

  private final ODALogger log = new ODALogger(this);
  private final RealmResource realm;
  private final KeycloakClient keycloak;
  private final String clientId;
  private final String clientSecret;

  @Inject
  public ChangePassword(
    RealmResource realm,
    KeycloakClient keycloak,
    @Value("${client.id}") String clientId,
    @Value("${client.secret}") String clientSecret
  ) {
    this.realm = realm;
    this.keycloak = keycloak;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Post("/recipients/commands/changePassword")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<Void>> changePassword(
    Authentication auth,
    @Body ChangePasswordCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    final Optional<UserRepresentation> user = realm
      .users()
      .search(owner.get(), true)
      .stream()
      .findFirst();
    if (user.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.notFound());
    }
    return keycloak
      .getAccessToken(
        Map.of(
          "client_id",
          clientId,
          "grant_type",
          "password",
          "client_secret",
          clientSecret,
          "scope",
          "openid",
          "username",
          owner.get(),
          "password",
          command.oldPassword()
        )
      )
      .thenApply(token -> {
        if (token.accessToken().isEmpty()) {
          throw Problem.builder().withTitle("Invalid credentials").build();
        }
        log.debug("Changing password", Map.of("recipientId", owner.get()));
        CredentialRepresentation credential = new CredentialRepresentation();
        var credId = realm.users().get(user.get().getId()).credentials().get(0).getId();
        log.debug(
          "Found credentials to reset",
          Map.of("userId", user.get().getId(), "credId", credId)
        );
        credential.setId(credId);
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(command.newPassword());
        realm.users().get(user.get().getId()).resetPassword(credential);
        log.info(
          "Password changed",
          Map.of(
            "recipientId",
            owner.get(),
            "userId",
            user.get().getId(),
            "credId",
            credId
          )
        );
        return HttpResponse.ok();
      });
  }

  @Serdeable
  public static record ChangePasswordCommand(
    String oldPassword,
    String newPassword
  ) {}
}
