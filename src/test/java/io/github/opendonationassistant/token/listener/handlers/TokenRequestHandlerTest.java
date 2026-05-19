package io.github.opendonationassistant.token.listener.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.rabbit.TokenRPC.TokenRequest;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.github.opendonationassistant.token.repository.TwitchToken;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TokenRequestHandlerTest {

  TokenRepository tokenRepository = Mockito.mock(TokenRepository.class);
  TokenRequestHandler handler = new TokenRequestHandler(tokenRepository);
  TwitchToken twitchToken = Mockito.mock(TwitchToken.class);

  @Test
  public void test() throws IOException {
    Mockito.when(twitchToken.data()).thenReturn(
      new TokenData(
        "refreshTokenId",
        "TOKEN",
        "refreshToken",
        "testuser",
        "Twitch",
        true,
        Map.of()
      )
    );

    Mockito.when(twitchToken.obtainAccessToken()).thenReturn(
      CompletableFuture.completedFuture("accessToken")
    );

    Mockito.when(tokenRepository.findById(Mockito.anyString())).thenReturn(
      Optional.of(twitchToken)
    );

    var response = handler.handle(
      new TokenRequest("testuser", "refreshTokenId")
    );
    assertEquals("accessToken", response.token());
    assertTrue(response.message().isEmpty());
  }

}
