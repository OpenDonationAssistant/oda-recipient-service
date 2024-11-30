package io.github.stcarolas.oda.otp.command;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class CreateOtpCommandResponse {

  private final String otp;

  public CreateOtpCommandResponse(String otp) {
    this.otp = otp;
  }

  public String getOtp() {
    return otp;
  }
}
