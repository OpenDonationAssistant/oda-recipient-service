package io.github.opendonationassistant.recipient.commands;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.integration.keycloak.KeycloakClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;

@Controller
public class UpdateEmail extends BaseController {

  private final KeycloakClient keycloak;
  private final RealmResource realm;

  @Inject
  public UpdateEmail(KeycloakClient keycloak, RealmResource realm) {
    this.keycloak = keycloak;
    this.realm = realm;
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Post("/recipients/commands/update-email")
  public CompletableFuture<HttpResponse<Void>> updateEmail(
    Authentication auth,
    @Body UpdateEmailCommand command
  ) {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    var users = realm.users().search(owner.get(), true);
    if (users.size() == 0) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    UserRepresentation user = users.getFirst();
    user.setEmail(command.email());
    return keycloak
      .updateUser(user.getId(), user)
      .thenCompose(v ->
        keycloak.sendVerifyEmail(user.getId(), Map.of("id", user.getId()))
      )
      .thenApply(v -> HttpResponse.ok());
  }

  @Serdeable
  public static record UpdateEmailCommand(String email) {}
}
