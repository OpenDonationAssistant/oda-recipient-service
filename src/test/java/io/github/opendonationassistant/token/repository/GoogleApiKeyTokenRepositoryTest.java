package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;

import io.github.opendonationassistant.token.repository.GoogleApiKeyToken.Settings;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone")
public class GoogleApiKeyTokenRepositoryTest {

  @Inject
  GoogleApiKeyTokenRepository googleApiKeyTokenRepository;

  @Inject
  TokenRepository tokenRepository;

  @Test
  public void testCreatingGoogleApiKeyToken() {
    googleApiKeyTokenRepository.create(
      "google-api-key",
      "testuser",
      new Settings()
    );
    var expected = tokenRepository
      .findByRecipientId("testuser")
      .stream()
      .findFirst();
    assertTrue(expected.isPresent());
    assertEquals("google-api-key", expected.get().data().token());
    assertEquals("GoogleApiKey", expected.get().data().system());
    assertEquals("accessToken", expected.get().data().type());
    assertEquals(Map.of(), expected.get().data().settings());
  }
}

