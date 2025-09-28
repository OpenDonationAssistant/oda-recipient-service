package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.token.command.DeleteToken.DeleteTokenCommand;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.micronaut.security.authentication.Authentication;
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
public class DeleteTokenTest {

  @WithSettings
  private final Settings settings = Settings.create()
    .mapType(Object.class, String.class);

  @Inject
  DeleteToken controller;

  @Inject
  TokenDataRepository repository;

  @Test
  public void testTokenDeletion(@Given TokenData token) {
    var auth = mock(Authentication.class);
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", token.recipientId())
    );

    repository.save(token);
    controller.deleteToken(auth, new DeleteTokenCommand(token.id()));
    var result = repository.findById(token.id());
    assertTrue(result.isEmpty());
  }
}
