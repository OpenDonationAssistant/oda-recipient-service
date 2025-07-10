package io.github.opendonationassistant.otp.command;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ExchangeOtpCommandResponse(String refreshToken) {}
