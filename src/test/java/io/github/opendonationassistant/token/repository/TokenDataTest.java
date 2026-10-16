package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class TokenDataTest {

  private final TokenData data = new TokenData(
    "id",
    "token",
    "refreshToken",
    "recipientId",
    "Twitch",
    true,
    false,
    Map.of("stored", "value"),
    List.of("scope")
  );

  @Test
  public void testWithEnabledChangesOnlyEnabled() {
    var expected = new TokenData(
      "id",
      "token",
      "refreshToken",
      "recipientId",
      "Twitch",
      false,
      false,
      Map.of("stored", "value"),
      List.of("scope")
    );

    assertEquals(expected, data.withEnabled(false));
  }

  @Test
  public void testWithDeletedChangesOnlyDeleted() {
    var expected = new TokenData(
      "id",
      "token",
      "refreshToken",
      "recipientId",
      "Twitch",
      true,
      true,
      Map.of("stored", "value"),
      List.of("scope")
    );

    assertEquals(expected, data.withDeleted(true));
  }

  @Test
  public void testWithTokenChangesOnlyToken() {
    var expected = new TokenData(
      "id",
      "new-token",
      "refreshToken",
      "recipientId",
      "Twitch",
      true,
      false,
      Map.of("stored", "value"),
      List.of("scope")
    );

    assertEquals(expected, data.withToken("new-token"));
  }

  @Test
  public void testWithSettingsChangesOnlySettings() {
    var expected = new TokenData(
      "id",
      "token",
      "refreshToken",
      "recipientId",
      "Twitch",
      true,
      false,
      Map.of("other", "settings"),
      List.of("scope")
    );

    assertEquals(
      expected,
      data.withSettings(Map.of("other", "settings"))
    );
  }

  @Test
  public void testWithScopesChangesOnlyScopes() {
    var expected = new TokenData(
      "id",
      "token",
      "refreshToken",
      "recipientId",
      "Twitch",
      true,
      false,
      Map.of("stored", "value"),
      List.of("other-scope")
    );

    assertEquals(expected, data.withScopes(List.of("other-scope")));
  }
}
