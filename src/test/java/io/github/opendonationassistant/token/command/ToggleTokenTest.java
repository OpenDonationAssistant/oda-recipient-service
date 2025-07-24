package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.token.command.DeleteToken.DeleteTokenCommand;
import io.github.opendonationassistant.token.command.ToggleToken.ToggleTokenCommand;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class ToggleTokenTest {

  @Inject
  TokenDataRepository repository;

  @Inject
  ToggleToken controller;

  @Test
  public void testTogglingToken(@Given TokenData token) {
    var auth = mock(Authentication.class);
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", token.recipientId())
    );

    repository.save(token);
    controller.toggleToken(auth, new ToggleTokenCommand(token.id(), false));
    var result = repository.findById(token.id());
    assertNotEquals(token.enabled(), result.get().enabled());
  }
}
