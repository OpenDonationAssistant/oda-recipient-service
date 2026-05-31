package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.token.repository.VkliveToken.Settings;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class TokenRepositoryTest {

  @Inject
  VkliveTokenRepository vkliveTokenRepository;

  @Inject
  TokenRepository tokenRepository;

  @Test
  public void testCreatingVkliveToken() {
    vkliveTokenRepository.create(
      "token",
      "testuser",
      new Settings("vkid", "vkname", "vkavatar")
    );
    var expected = tokenRepository
      .findByRecipientId("testuser")
      .stream()
      .findFirst();
    assertTrue(expected.isPresent());
    assertEquals("token", expected.get().data().token());
    assertEquals("VKlive", expected.get().data().system());
    assertEquals("vkid", expected.get().data().settings().get("id"));
    assertEquals("vkname", expected.get().data().settings().get("name"));
    assertEquals("vkavatar", expected.get().data().settings().get("avatar"));
  }
}
