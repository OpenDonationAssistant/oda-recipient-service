package io.github.opendonationassistant.token.command;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.command.DeleteToken.DeleteTokenCommand;
import io.github.opendonationassistant.token.repository.KickToken;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.rabbitmq.connect.ChannelPool;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.TransactionOperations;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.WithSettings;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
@WireMockTest(httpPort = 8080)
public class DeleteTokenTest {

  private static ODALogger log = new ODALogger(DeleteTokenTest.class);

  @WithSettings
  private final Settings settings = Settings.create()
    .mapType(Object.class, String.class);

  Authentication auth = mock(Authentication.class);

  // @BeforeAll
  // static void setup(WireMockRuntimeInfo wiremock) {
  //   var baseUrl = wiremock.getHttpBaseUrl();
  //   log.info("Wiremock started", Map.of("url", baseUrl));
  //   System.setProperty("MICRONAUT_HTTP_SERVICES_KICK-AUTH_URL", baseUrl);
  // }

  @Inject
  DeleteToken controller;

  @Inject
  @Named("commands")
  RabbitClient client;

  @Inject
  ChannelPool channel;

  @Inject
  TokenDataRepository repository;

  @Inject
  TokenRepository tokenRepository;

  @Inject
  ObjectMapper mapper;

  @Test
  @Disabled
  public void testGenericTokenDeletion(@Given TokenData token)
    throws IOException, InterruptedException {
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", token.recipientId())
    );

    log.debug("Creating token", Map.of("token", token));
    repository.save(token);
    controller
      .deleteToken(auth, new DeleteTokenCommand(token.id()))
      // .thenAccept(result -> assertFalse(repository.existsById(token.id())))
      .join();
    controller.deleteToken(auth, new DeleteTokenCommand(token.id()))
      .thenAccept(result ->
        assertEquals(HttpStatus.NOT_FOUND, result.getStatus())
      )
      .join();
  }

  @Test
  @Disabled
  public void testKickTokenDeletion(@Given TokenData template)
    throws IOException {
    String json =
      """
        {
          "access_token": "access_token",
          "refresh_token": "refresh_token"
        }
      """;
    stubFor(post("/oauth/token").willReturn(ok(json)));

    var queue = channel.getChannel().queueDeclare().getQueue();
    channel.getChannel().queueBind(queue, "commands", "*");

    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", template.recipientId())
    );

    var token = new TokenData(
      template.id(),
      template.token(),
      template.type(),
      template.recipientId(),
      "Kick",
      false,
      template.settings()
    );

    log.debug("Creating token", Map.of("token", token));
    repository.save(token);
    controller
      .deleteToken(auth, new DeleteTokenCommand(token.id()))
      .thenAccept(result -> assertFalse(repository.existsById(token.id())))
      .join();

    var message = channel.getChannel().basicGet(queue, true);
    assertNotNull(message);
    var command = ObjectMapper.getDefault()
      .readValue(
        message.getBody(),
        KickToken.UnsubscribeKickEventsCommand.class
      );
    log.debug("Received command", Map.of("command", command));
    assertNotNull(command);
    assertEquals(token.recipientId(), command.recipientId());
    assertEquals(token.id(), command.refreshTokenId());
    assertNotNull(command.token());
  }
}
