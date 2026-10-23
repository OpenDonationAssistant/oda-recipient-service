package io.github.opendonationassistant.integration.vklive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.integration.vklive.VKLiveClient.VKLiveTokenException;
import io.github.opendonationassistant.integration.vklive.VKLiveClient.VKLiveTokenResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VKLiveClientTest {

  private VKLiveClient.VKLiveClientAuthApi auth;
  private VKLiveClient client;

  @BeforeEach
  public void setUp() {
    auth = mock(VKLiveClient.VKLiveClientAuthApi.class);
    client = new VKLiveClient(
      auth,
      mock(VKLiveClient.VKLiveClientDataApi.class),
      "https://widgets.oda.digital",
      "client-id",
      "client-secret"
    );
  }

  @Test
  public void testObtainsAccessTokenFromSuccessfulResponse() {
    givenResponse(
      HttpResponse.ok(
        new VKLiveTokenResponse("access-token", "refresh-token", null, null)
      )
    );

    var tokens = client.obtainAccessToken("old-refresh-token").join();

    assertEquals("access-token", tokens.accessToken());
    assertEquals("refresh-token", tokens.refreshToken());
  }

  @Test
  public void testFailsWithMeaningfulExceptionWhenResponseIsNull() {
    when(auth.getToken(anyString(), anyMap())).thenReturn(
      CompletableFuture.completedFuture(null)
    );

    var message = messageOf(assertRefreshFailure());

    assertTrue(message.contains("no response"));
  }

  @Test
  public void testFailsWithMeaningfulExceptionWhenBodyIsEmpty() {
    givenResponse(HttpResponse.ok());

    var message = messageOf(assertRefreshFailure());

    assertTrue(message.contains("empty response"));
    assertTrue(message.contains("200"));
  }

  @Test
  public void testFailsWithStatusAndProviderErrorWhenClientThrows() {
    givenFailure(
      new HttpClientResponseException(
        "Unauthorized",
        HttpResponse
          .status(HttpStatus.UNAUTHORIZED)
          .body(
            new VKLiveTokenResponse(
              null,
              null,
              "invalid_grant",
              "Refresh token is invalid"
            )
          )
      )
    );

    var message = messageOf(assertRefreshFailure());

    assertTrue(message.contains("401"));
    assertTrue(message.contains("invalid_grant"));
    assertTrue(message.contains("Refresh token is invalid"));
  }

  @Test
  public void testFailsWhenSuccessStatusContainsProviderError() {
    givenResponse(
      HttpResponse.ok(
        new VKLiveTokenResponse(
          null,
          null,
          "invalid_request",
          "Missing refresh token"
        )
      )
    );

    var message = messageOf(assertRefreshFailure());

    assertTrue(message.contains("invalid_request"));
    assertTrue(message.contains("Missing refresh token"));
  }

  @Test
  public void testFailsWithTransportErrorMessage() {
    givenFailure(new IllegalStateException("connection refused"));

    var message = messageOf(assertRefreshFailure());

    assertTrue(message.contains("no response"));
    assertTrue(message.contains("connection refused"));
  }

  @Test
  public void testLinkReturnsTokensFromSuccessfulResponse() {
    givenResponse(
      HttpResponse.ok(
        new VKLiveTokenResponse("access-token", "refresh-token", null, null)
      )
    );

    var response = client.link("authorization-code").join();

    assertEquals("access-token", response.accessToken());
    assertEquals("refresh-token", response.refreshToken());
  }

  @Test
  public void testLinkFailsWithMeaningfulExceptionWhenBodyIsEmpty() {
    givenResponse(HttpResponse.ok());

    var message = messageOf(
      assertInstanceOf(
        VKLiveTokenException.class,
        assertThrows(
          CompletionException.class,
          () -> client.link("authorization-code").join()
        ).getCause()
      )
    );

    assertTrue(message.contains("link"));
    assertTrue(message.contains("empty response"));
  }

  private void givenResponse(HttpResponse<VKLiveTokenResponse> response) {
    when(auth.getToken(anyString(), anyMap())).thenReturn(
      CompletableFuture.completedFuture(response)
    );
  }

  private void givenFailure(Throwable failure) {
    when(auth.getToken(anyString(), anyMap())).thenReturn(
      CompletableFuture.failedFuture(failure)
    );
  }

  private VKLiveTokenException assertRefreshFailure() {
    var cause = assertThrows(
      CompletionException.class,
      () -> client.obtainAccessToken("old-refresh-token").join()
    ).getCause();
    return assertInstanceOf(VKLiveTokenException.class, cause);
  }

  private static String messageOf(VKLiveTokenException exception) {
    return String.valueOf(exception.getMessage());
  }
}
