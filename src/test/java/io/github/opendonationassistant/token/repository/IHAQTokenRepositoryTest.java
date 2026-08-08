package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;

import io.github.opendonationassistant.token.repository.IHAQToken.Settings;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone")
public class IHAQTokenRepositoryTest {

  @Inject
  IHAQTokenRepository ihaqTokenRepository;

  @Inject
  TokenRepository tokenRepository;

  @Test
  public void testCreatingIHAQToken() {
    ihaqTokenRepository.create(
      "refresh-token",
      "testuser",
      new Settings("ihaq-id", "ihaq-username", "ihaq-api-token")
    );
    var expected = tokenRepository
      .findByRecipientId("testuser")
      .stream()
      .findFirst();
    assertTrue(expected.isPresent());
    assertEquals("refresh-token", expected.get().data().token());
    assertEquals("IHAQ", expected.get().data().system());
    assertEquals("refreshToken", expected.get().data().type());
    assertEquals("ihaq-id", expected.get().data().settings().get("id"));
    assertEquals(
      "ihaq-username",
      expected.get().data().settings().get("username")
    );
    assertEquals(
      "ihaq-api-token",
      expected.get().data().settings().get("apiToken")
    );
    assertEquals(true, expected.get().data().settings().get("triggerAlerts"));
    assertEquals(true, expected.get().data().settings().get("triggerDonaton"));
    assertEquals(true, expected.get().data().settings().get("triggerReel"));
    assertEquals(true, expected.get().data().settings().get("addToGoal"));
    assertEquals(true, expected.get().data().settings().get("countInTop"));
  }
}
