package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class SetTokenTest {

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
        assertTrue(tokens.getBody().isPresent());
        assertEquals(1, tokens.getBody().get().size());
        assertEquals(id, tokens.getBody().get().get(0).id());
        assertEquals(token, tokens.getBody().get().get(0).token());
        assertEquals("DonateX", tokens.getBody().get().get(0).system());
        assertEquals("accessToken", tokens.getBody().get().get(0).type());
      });
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

    setToken
      .setToken(auth, createCommand)
      .thenCompose(it -> setToken.setToken(auth, updateCommand))
      .thenRun(() -> {
        var tokens = tokenController.listTokens(auth);
        assertTrue(tokens.getBody().isPresent());
        assertEquals(1, tokens.getBody().get().size());
        assertEquals(command.token(), tokens.getBody().get().get(0).token());
        assertEquals(command.system(), tokens.getBody().get().get(0).system());
        assertEquals(command.type(), tokens.getBody().get().get(0).type());
      });
  }
}
