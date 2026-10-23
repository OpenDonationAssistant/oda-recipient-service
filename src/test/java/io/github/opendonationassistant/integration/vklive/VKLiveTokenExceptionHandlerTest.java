package io.github.opendonationassistant.integration.vklive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.opendonationassistant.integration.vklive.VKLiveClient.VKLiveTokenException;
import io.micronaut.http.HttpRequest;
import org.junit.jupiter.api.Test;

public class VKLiveTokenExceptionHandlerTest {

  @Test
  public void testReturnsBadGatewayForFailedTokenRequest() {
    var handler = new VKLiveTokenExceptionHandler();

    var response = handler.handle(
      HttpRequest.GET("/recipients/tokens/get-access-token"),
      new VKLiveTokenException("VK Live token request failed")
    );

    assertEquals(502, response.getStatus().getCode());
  }
}
