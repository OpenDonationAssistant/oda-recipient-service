package io.github.opendonationassistant.recipient.commands;

import io.github.opendonationassistant.events.files.CreateBucketCommand;
import io.github.opendonationassistant.events.files.FilesCommandSender;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@Controller
public class CreateRecipient {

  public final FilesCommandSender sender;
  public final RealmResource realm;

  @Inject
  public CreateRecipient(FilesCommandSender sender, RealmResource realm) {
    this.sender = sender;
    this.realm = realm;
  }

  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post("/recipients/create")
  public HttpResponse<CreateRecipientCommandResponse> createRecipient(
    @Body CreateRecipientCommand command
  ) {
    var newUser = new UserRepresentation();
    newUser.setEnabled(true);
    newUser.setUsername(command.nickname());
    // newUser.setEmail("some@mail.ru");
    // newUser.setAttributes(
    //   Collections.singletonMap("origin", Arrays.asList("demo"))
    // );
    var password = generateCommonLangPassword();
    newUser.setCredentials(List.of(passwordRepresentation(password)));
    var newUserResponse = realm.users().create(newUser);
    CompletableFuture.runAsync(
      () ->
        sender.sendCreateBucketCommand(
          new CreateBucketCommand(command.nickname())
        ),
      Executors.newSingleThreadExecutor()
    );
    return HttpResponse.ok(new CreateRecipientCommandResponse(password));
  }

  private CredentialRepresentation passwordRepresentation(String password) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setTemporary(false);
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    return credential;
  }

  private String generateCommonLangPassword() {
    String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
    String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
    String numbers = RandomStringUtils.randomNumeric(2);
    String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
    String totalChars = RandomStringUtils.randomAlphanumeric(2);
    String combinedChars = upperCaseLetters
      .concat(lowerCaseLetters)
      .concat(numbers)
      .concat(specialChar)
      .concat(totalChars);
    List<Character> pwdChars = combinedChars
      .chars()
      .mapToObj(c -> (char) c)
      .collect(Collectors.toList());
    Collections.shuffle(pwdChars);
    String password = pwdChars
      .stream()
      .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
      .toString();
    return password;
  }

  @Serdeable
  public static record CreateRecipientCommand(String nickname) {}

  @Serdeable
  public static record CreateRecipientCommandResponse(String password) {}
}
