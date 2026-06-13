package io.github.opendonationassistant.token.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.rabbit.TokenRPC.TokenRequest;
import io.github.opendonationassistant.rabbit.TokenRPC.TokenResponse;
import io.github.opendonationassistant.token.repository.RefreshToken;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;

@RabbitListener
public class TokenRequestHandler {

  private ODALogger log = new ODALogger(this);
  public static final String QUEUE_NAME = "recipient.token-request";
  public static final io.github.opendonationassistant.rabbit.Queue QUEUE =
    new io.github.opendonationassistant.rabbit.Queue(QUEUE_NAME);
  private final TokenRepository tokenRepository;

  @Inject
  public TokenRequestHandler(TokenRepository tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  @Queue(QUEUE_NAME)
  public TokenResponse handle(TokenRequest message) throws IOException {
    try {
      log.debug(
        "Received token request",
        Map.of(
          "recipientId",
          message.recipientId(),
          "refreshTokenId",
          message.refreshTokenId()
        )
      );
      String obtainedToken = tokenRepository
        .findById(message.refreshTokenId())
        .filter(token ->
          token.data().recipientId().equals(message.recipientId())
        )
        .filter(token -> token instanceof RefreshToken)
        .map(token ->
          ((RefreshToken) token).obtainAccessToken().join().accessToken()
        )
        .orElse(null);
      if (obtainedToken == null) {
        log.warn(
          "Token can't be obtained",
          Map.of(
            "recipientId",
            message.recipientId(),
            "refreshTokenId",
            message.refreshTokenId()
          )
        );
        return new TokenResponse(null, "Token not found");
      }
      return new TokenResponse(obtainedToken, "");
    } catch (Exception e) {
      log.error(
        "Failed to obtain token",
        Map.of(
          "recipientId",
          message.recipientId(),
          "refreshTokenId",
          message.refreshTokenId(),
          "error",
          e.getMessage()
        )
      );
      return new TokenResponse(null, e.getMessage());
    }
  }
}
