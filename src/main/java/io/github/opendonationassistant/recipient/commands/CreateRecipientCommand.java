package io.github.opendonationassistant.recipient.commands;

import io.github.opendonationassistant.events.files.CreateBucketCommand;
import io.github.opendonationassistant.events.files.FilesCommandSender;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class CreateRecipientCommand {

  public final String nickname;
  public final FilesCommandSender sender;
  public final RealmResource realm;

  public CreateRecipientCommand(
    FilesCommandSender sender,
    RealmResource realm,
    String nickname
  ) {
    this.nickname = nickname;
    this.sender = sender;
    this.realm = realm;
  }

  public void execute() {
    var newUser = new UserRepresentation();
    newUser.setEnabled(true);
    newUser.setUsername(nickname);
    // newUser.setEmail("some@mail.ru");
    // newUser.setAttributes(
    //   Collections.singletonMap("origin", Arrays.asList("demo"))
    // );
    newUser.setCredentials(List.of(generatePassword()));
    var newUserResponse = realm.users().create(newUser);
    CompletableFuture.runAsync(
      () -> sender.sendCreateBucketCommand(new CreateBucketCommand(nickname)),
      Executors.newSingleThreadExecutor()
    );
  }

  private CredentialRepresentation generatePassword() {
    var password = generateCommonLangPassword();
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
}
