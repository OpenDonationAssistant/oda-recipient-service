package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.repository.OauthClient.RefreshedTokens;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

public class RefreshTokenTest {

  private final OauthClient oauth = mock(OauthClient.class);
  private final TokenDataRepository repository = mock(
    TokenDataRepository.class
  );
  private final RabbitClient events = mock(RabbitClient.class);

  private final TokenData data = new TokenData(
    "id",
    "old-refresh",
    "refreshToken",
    "recipientId",
    "Twitch",
    true,
    false,
    Map.of(),
    List.of()
  );

  @Test
  public void testObtainAccessTokenPersistsRefreshedRefreshToken() {
    when(oauth.obtainAccessToken("old-refresh"))
      .thenReturn(
        CompletableFuture.completedFuture(
          new RefreshedTokens("new-access", "new-refresh")
        )
      );
    when(repository.existsById("id")).thenReturn(true);
    var token = new RefreshToken(oauth, data, repository, events);

    var result = token.obtainAccessToken().join();

    assertEquals("new-access", result.accessToken());
    assertEquals("new-refresh", result.refreshToken());
    assertEquals("new-refresh", token.data().token());
    verify(repository).update(any(TokenData.class));
  }

  @Test
  public void testObtainAccessTokenPropagatesOauthFailure() {
    when(oauth.obtainAccessToken(any()))
      .thenReturn(
        CompletableFuture.failedFuture(
          new IllegalStateException("oauth unavailable")
        )
      );
    var token = new RefreshToken(oauth, data, repository, events);

    assertThrows(
      CompletionException.class,
      () -> token.obtainAccessToken().join()
    );
    verify(repository, never()).update(any(TokenData.class));
  }
}
