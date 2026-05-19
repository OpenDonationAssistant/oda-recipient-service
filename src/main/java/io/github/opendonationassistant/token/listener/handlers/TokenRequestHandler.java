package io.github.opendonationassistant.token.listener.handlers;

import io.github.opendonationassistant.rabbit.TokenRPC.TokenRequest;
import io.github.opendonationassistant.rabbit.TokenRPC.TokenResponse;
import io.github.opendonationassistant.token.repository.RefreshToken;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.io.IOException;

@RabbitListener
public class TokenRequestHandler {

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
    String obtainedToken = tokenRepository
      .findById(message.refreshTokenId())
      .filter(token -> token.data().recipientId().equals(message.recipientId()))
      .filter(token -> token instanceof RefreshToken)
      .map(token -> ((RefreshToken) token).obtainAccessToken().join())
      .orElse(null);
    return new TokenResponse(obtainedToken, "");
  }
}
