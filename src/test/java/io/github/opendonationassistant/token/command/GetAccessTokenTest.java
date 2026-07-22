package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

import io.github.opendonationassistant.token.command.GetAccessToken.GetAccessTokenCommand;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.WithSettings;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.MediaType;

@MicronautTest(environments = "allinone", transactional = false)
@ExtendWith(InstancioExtension.class)
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = { 8080 })
public class GetAccessTokenTest {

  @WithSettings
  private final Settings settings = Settings.create()
    .mapType(Object.class, String.class);

  Authentication auth = mock(Authentication.class);

  @Inject
  GetAccessToken controller;

  @Inject
  TokenDataRepository repository;

  @Inject
  TokenRepository tokenRepository;

  @Test
  public void testGetAccessTokenForGenericToken(@Given String recipientId) {
    var tokenData = Instancio.of(TokenData.class)
      .set(Select.field(TokenData::type), "accessToken")
      .set(Select.field(TokenData::recipientId), recipientId)
      .set(Select.field(TokenData::enabled), true)
      .set(Select.field(TokenData::deleted), false)
      .set(Select.field(TokenData::system), "Generic")
      .create();

    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", recipientId)
    );

    repository.save(tokenData);

    var response = controller
      .getAccessToken(auth, new GetAccessTokenCommand(tokenData.id()))
      .join();

    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals(tokenData.token(), body.token());
  }

  @Test
  public void testGetAccessTokenForKickRefreshToken(
    @Given String recipientId,
    MockServerClient client
  ) {
    client
      .when(request().withPath("/oauth/token"))
      .respond(
        response()
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(
            """
            {
              "access_token": "refreshed_access_token",
              "refresh_token": "new_refresh_token"
            }
            """
          )
      );

    var tokenData = Instancio.of(TokenData.class)
      .set(Select.field(TokenData::type), "refreshToken")
      .set(Select.field(TokenData::recipientId), recipientId)
      .set(Select.field(TokenData::enabled), true)
      .set(Select.field(TokenData::deleted), false)
      .set(Select.field(TokenData::system), "Kick")
      .create();

    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", recipientId)
    );

    repository.save(tokenData);

    var response = controller
      .getAccessToken(auth, new GetAccessTokenCommand(tokenData.id()))
      .join();

    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals("refreshed_access_token", body.token());
  }

  @Test
  public void testReturnsUnauthorizedWhenOwnerIsEmpty(
    @Given GetAccessTokenCommand command
  ) {
    when(auth.getAttributes()).thenReturn(Map.of());

    var response = controller.getAccessToken(auth, command).join();

    assertEquals(401, response.getStatus().getCode());
  }

  @Test
  public void testReturnsUnauthorizedWhenTokenNotFound(
    @Given String recipientId,
    @Given String tokenId
  ) {
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", recipientId)
    );

    var response = controller
      .getAccessToken(auth, new GetAccessTokenCommand(tokenId))
      .join();

    assertEquals(401, response.getStatus().getCode());
  }

  @Test
  public void testReturnsUnauthorizedWhenTokenNotOwnedByUser(
    @Given String recipientId,
    @Given String otherRecipientId
  ) {
    var tokenData = Instancio.of(TokenData.class)
      .set(Select.field(TokenData::recipientId), otherRecipientId)
      .set(Select.field(TokenData::enabled), true)
      .set(Select.field(TokenData::deleted), false)
      .set(Select.field(TokenData::system), "Generic")
      .create();

    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", recipientId)
    );

    repository.save(tokenData);

    var response = controller
      .getAccessToken(auth, new GetAccessTokenCommand(tokenData.id()))
      .join();

    assertEquals(401, response.getStatus().getCode());
  }
}
