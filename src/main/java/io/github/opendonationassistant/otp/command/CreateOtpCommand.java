package io.github.opendonationassistant.otp.command;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.UUID;

@Serdeable
public class CreateOtpCommand {

  private final String refreshToken;

  public CreateOtpCommand(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public CreateOtpCommandResponse execute(Map<String, String> cache) {
    var otp = UUID.randomUUID().toString();
    cache.put(otp, refreshToken);
    return new CreateOtpCommandResponse(otp);
  }
}
