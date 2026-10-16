package io.github.opendonationassistant.info;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micronaut.security.authentication.Authentication;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class InfoControllerTest {

  private final InfoController controller = new InfoController();

  @Test
  public void testReturnsNotLoggedInWhenAuthenticationIsMissing() {
    var response = controller.test(null);

    var body = response.body();
    assertNotNull(body);
    assertFalse(body.logged());
    assertEquals("", body.id());
  }

  @Test
  public void testReturnsLoggedInWithUsernameWhenAuthenticated() {
    var auth = mock(Authentication.class);
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", "alice")
    );

    var response = controller.test(auth);

    var body = response.body();
    assertNotNull(body);
    assertTrue(body.logged());
    assertEquals("alice", body.id());
  }

  @Test
  public void testReturnsEmptyIdWhenUsernameAttributeIsMissing() {
    var auth = mock(Authentication.class);
    when(auth.getAttributes()).thenReturn(Map.of());

    var response = controller.test(auth);

    var body = response.body();
    assertNotNull(body);
    assertTrue(body.logged());
    assertEquals("", body.id());
  }
}
