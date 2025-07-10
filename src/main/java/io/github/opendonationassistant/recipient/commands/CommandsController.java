package io.github.opendonationassistant.recipient.commands;

import io.github.opendonationassistant.events.files.FilesCommandSender;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.keycloak.admin.client.resource.RealmResource;

@Controller
public class CommandsController {

  public final FilesCommandSender sender;
  public final RealmResource realm;

  @Inject
  public CommandsController(FilesCommandSender sender, RealmResource realm) {
    this.sender = sender;
    this.realm = realm;
  }

  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post("/admin/createUser/{nickname}")
  public void create(String nickname) {
    new CreateRecipientCommand(sender, realm, nickname).execute();
  }
}
