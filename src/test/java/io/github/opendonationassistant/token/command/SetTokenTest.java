package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.*;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.testutils.AuthenticationGenerator;
import io.github.opendonationassistant.token.command.SetToken.SetTokenCommand;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.github.opendonationassistant.token.view.TokenController;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.WithSettings;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone", transactional = false)
@ExtendWith(InstancioExtension.class)
public class SetTokenTest {

  private ODALogger log = new ODALogger(this);

  @WithSettings
  private final Settings settings = Settings.create()
    .mapType(Object.class, String.class);

  @Inject
  SetToken setToken;

  @Inject
  TokenController tokenController;

  @Inject
  TokenDataRepository repository;

  @Test
  public void testCreatingNewToken(
    @Given String id,
    @Given String token,
    @Given String recipientId
  ) {
    var createCommand = new SetTokenCommand(
      id,
      token,
      "accessToken",
      "DonateX",
      Map.of()
    );
    var auth = AuthenticationGenerator.forUser(recipientId);

    setToken
      .setToken(auth, createCommand)
      .thenRun(() -> {
        var tokens = tokenController.listTokens(auth);
        assertTrue(tokens.getBody().isPresent(), "Token was created");
        assertEquals(1, tokens.getBody().get().size(), "Exactly one token");
        assertEquals(
          id,
          tokens.getBody().get().get(0).id(),
          "Token id correct"
        );
        assertEquals(
          token,
          tokens.getBody().get().get(0).token(),
          "Token itself correct"
        );
        assertEquals(
          "DonateX",
          tokens.getBody().get().get(0).system(),
          "System correct"
        );
        assertEquals(
          "accessToken",
          tokens.getBody().get().get(0).type(),
          "Type correct"
        );
        var shouldBeEmpty = tokenController.listTokens(
          AuthenticationGenerator.forUser("wrongUser")
        );
        assertTrue(
          shouldBeEmpty.getBody().get().isEmpty(),
          "Other user shouldn't see it"
        );
      })
      .join();
  }

  @Test
  public void testUpdatingToken(
    @Given SetTokenCommand command,
    @Given String recipientId,
    @Given TokenData oldData
  ) {
    var auth = AuthenticationGenerator.forUser(recipientId);

    var createCommand = new SetTokenCommand(
      oldData.id(),
      oldData.token(),
      oldData.type(),
      "DonateX",
      oldData.settings()
    );

    var updateCommand = new SetTokenCommand(
      oldData.id(),
      command.token(),
      command.type(),
      "DonateX",
      command.settings()
    );

    var mergedSettings = oldData.settings();
    mergedSettings.putAll(command.settings());

    setToken
      .setToken(auth, createCommand)
      .thenCompose(it -> setToken.setToken(auth, updateCommand))
      .thenRun(() -> {
        var tokens = tokenController.listTokens(auth);
        log.debug("Tokens", Map.of("tokens", tokens.getBody().get()));
        assertTrue(tokens.getBody().isPresent());
        assertEquals(
          1,
          tokens.getBody().get().size(),
          "New token was not created"
        );
        assertEquals(
          oldData.token(),
          tokens.getBody().get().get(0).token(),
          "Token correct"
        );
        assertEquals(
          "DonateX",
          tokens.getBody().get().get(0).system(),
          "System correct"
        );
        assertEquals(
          oldData.type(),
          tokens.getBody().get().get(0).type(),
          "Type correct"
        );
        assertEquals(
          mergedSettings,
          tokens.getBody().get().get(0).settings(),
          "Settings correct"
        );
      })
      .join();
  }
}
