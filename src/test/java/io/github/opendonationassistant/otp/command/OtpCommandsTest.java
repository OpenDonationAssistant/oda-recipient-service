package io.github.opendonationassistant.otp.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class OtpCommandsTest {

  private final Map<String, String> cache = new HashMap<>();

  @Test
  public void testCreateStoresRefreshTokenUnderGeneratedOtp() {
    var response = new CreateOtpCommand("refresh-token").execute(cache);

    assertNotNull(response.getOtp());
    assertEquals("refresh-token", cache.get(response.getOtp()));
  }

  @Test
  public void testCreateGeneratesDistinctOtps() {
    var first = new CreateOtpCommand("first").execute(cache);
    var second = new CreateOtpCommand("second").execute(cache);

    assertNotEquals(first.getOtp(), second.getOtp());
    assertEquals(2, cache.size());
  }

  @Test
  public void testExchangeReturnsStoredRefreshToken() {
    var created = new CreateOtpCommand("refresh-token").execute(cache);

    var exchanged = new ExchangeOtpCommand(created.getOtp()).execute(cache);

    assertEquals("refresh-token", exchanged.refreshToken());
  }

  @Test
  public void testExchangeReturnsEmptyStringForUnknownOtp() {
    var exchanged = new ExchangeOtpCommand("unknown").execute(cache);

    assertEquals("", exchanged.refreshToken());
  }
}
