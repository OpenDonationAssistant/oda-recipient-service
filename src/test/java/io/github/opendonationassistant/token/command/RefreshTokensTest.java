package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.github.opendonationassistant.token.command.RefreshTokens.RefreshTokensCommand;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.WithSettings;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
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
public class RefreshTokensTest {

  @WithSettings
  private final Settings settings = Settings.create()
    .mapType(Object.class, String.class);

  @Inject
  RefreshTokens controller;

  @Inject
  TokenDataRepository repository;

  @BeforeEach
  public void cleanUpKickTokens() {
    repository.findBySystemAndDeletedFalse("Kick").forEach(repository::delete);
  }

  @Test
  public void testRefreshesRefreshTokensForSystem(
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
      .set(Select.field(TokenData::system), "Kick")
      .set(Select.field(TokenData::enabled), true)
      .set(Select.field(TokenData::deleted), false)
      .create();

    repository.save(tokenData);

    var response = controller
      .refresh(new RefreshTokensCommand("Kick"))
      .join();

    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals(1, body.size());
    assertEquals("refreshed_access_token", body.get(0).accessToken());
    assertEquals("new_refresh_token", body.get(0).refreshToken());

    repository.delete(tokenData);
  }

  @Test
  public void testReturnsStoredTokenForGenericToken(@Given String system) {
    var tokenData = Instancio.of(TokenData.class)
      .set(Select.field(TokenData::type), "accessToken")
      .set(Select.field(TokenData::system), system)
      .set(Select.field(TokenData::enabled), true)
      .set(Select.field(TokenData::deleted), false)
      .create();

    repository.save(tokenData);

    var response = controller
      .refresh(new RefreshTokensCommand(system))
      .join();

    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals(1, body.size());
    assertEquals(tokenData.token(), body.get(0).accessToken());
    assertEquals(tokenData.token(), body.get(0).refreshToken());

    repository.delete(tokenData);
  }

  @Test
  public void testReturnsEmptyListWhenNoTokensForSystem(@Given String system) {
    var response = controller
      .refresh(new RefreshTokensCommand(system))
      .join();

    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals(List.of(), body);
  }
}