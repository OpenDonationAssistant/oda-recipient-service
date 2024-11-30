package io.github.stcarolas.oda.otp.command;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ExchangeOtpCommandResponse {

  private final String refreshToken;

  public ExchangeOtpCommandResponse(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}
