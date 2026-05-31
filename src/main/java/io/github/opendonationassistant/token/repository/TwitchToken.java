package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.JsonConvertable;
import io.github.opendonationassistant.integration.twitch.TwitchClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TwitchToken extends RefreshToken {

  private RabbitClient rabbit;

  public TwitchToken(
    TwitchClient client,
    TokenData data,
    TokenDataRepository repository,
    RabbitClient rabbit
  ) {
    super(client, data, repository);
    this.rabbit = rabbit;
  }

  @Override
  public CompletableFuture<Void> delete() {
    rabbit.sendCommand(
      new UnsubscribeAllTwitchEventsCommand(data().recipientId(), data().id())
    );
    return super.delete();
  }

  @Serdeable
  public record Settings(String id, String name, String avatar, String email)
    implements JsonConvertable {
    public Map<String, Object> asJsonMap() {
      return Map.of("id", id, "name", name, "avatar", avatar, "email", email);
    }
  }

  @Serdeable
  public static record UnsubscribeAllTwitchEventsCommand(
    String recipientId,
    String refreshTokenId
  ) {}
}
