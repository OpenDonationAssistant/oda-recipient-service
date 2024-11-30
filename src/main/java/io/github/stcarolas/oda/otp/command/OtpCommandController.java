package io.github.stcarolas.oda.otp.command;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.Map;

@Controller("/otp")
public class OtpCommandController {

  private final Map<String, String> cache;

  public OtpCommandController(Map<String, String> cache) {
    this.cache = cache;
  }

  // TODO: Add authentication
  @Post("create")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public CreateOtpCommandResponse createOtp(@Body CreateOtpCommand command) {
    return command.execute(cache);
  }

  @Post("exchange")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public ExchangeOtpCommandResponse exchangeOtp(
    @Body ExchangeOtpCommand command
  ) {
    return command.execute(cache);
  }
}
