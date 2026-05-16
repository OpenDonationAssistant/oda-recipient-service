package io.github.opendonationassistant.token.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.token.command.DeleteToken.DeleteTokenCommand;
import io.github.opendonationassistant.token.repository.KickToken;
import io.github.opendonationassistant.token.repository.TokenData;
import io.github.opendonationassistant.token.repository.TokenDataRepository;
import io.github.opendonationassistant.token.repository.TokenRepository;
import io.micronaut.rabbitmq.connect.ChannelPool;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.Map;
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

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = { 8080 })
public class DeleteTokenTest {

  private static ODALogger log = new ODALogger(DeleteTokenTest.class);

  @WithSettings
  private final Settings settings = Settings.create()
    .mapType(Object.class, String.class);

  Authentication auth = mock(Authentication.class);

  @Inject
  DeleteToken controller;

  @Inject
  @Named("commands")
  RabbitClient rabbit;

  @Inject
  ChannelPool channel;

  @Inject
  TokenDataRepository repository;

  @Inject
  TokenRepository tokenRepository;

  @Inject
  ObjectMapper mapper;

  @Test
  public void testGenericTokenDeletion(@Given TokenData token)
    throws IOException, InterruptedException {
    when(auth.getAttributes()).thenReturn(
      Map.of("preferred_username", token.recipientId())
    );

    log.debug("Creating token", Map.of("token", token));
    repository.save(token);

    controller
      .deleteToken(auth, new DeleteTokenCommand(token.id()))
      .thenAccept(result -> assertFalse(repository.existsById(token.id())))
      .join();
  }

  @Test
  public void testKickTokenDeletion(
    @Given TokenData template,
    MockServerClient client
  ) throws IOException, InterruptedException {
    client
      .when(request().withPath("/oauth/token"))
      .respond(
        response()
          .withContentType(MediaType.APPLICATION_JSON)
          .withBody(
            """
              {
                "access_token": "access_token",
                "refresh_token": "refresh_token"
              }
            """
          )
      );

    var queue = channel.getChannel().queueDeclare().getQueue();
    channel
      .getChannel()
      .queueBind(queue, "commands", "command.UnsubscribeKickEventsCommand");

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
    assertNotNull(command);
    assertEquals("access_token", command.token());
    assertEquals(token.recipientId(), command.recipientId());
    assertEquals(token.id(), command.refreshTokenId());
  }
}
