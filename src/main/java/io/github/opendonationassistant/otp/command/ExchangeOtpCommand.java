package io.github.opendonationassistant.otp.command;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
public class ExchangeOtpCommand {

  private final String otp;

  public ExchangeOtpCommand(String otp) {
    this.otp = otp;
  }

  public ExchangeOtpCommandResponse execute(Map<String, String> cache) {
    var token = cache.get(otp);
    return new ExchangeOtpCommandResponse(token);
  }
}
