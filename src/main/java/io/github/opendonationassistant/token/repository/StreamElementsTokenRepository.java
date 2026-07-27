package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.streamelements.StreamElementsClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class StreamElementsTokenRepository
  extends GenericTokenProvider<
    StreamElementsToken, StreamElementsToken.Settings
  > {

  private final RabbitClient events;
  private final StreamElementsClient client;

  @Inject
  public StreamElementsTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    StreamElementsClient client
  ) {
    super(repository);
    this.events = events;
    this.client = client;
  }

  @Override
  public String system() {
    return "StreamElements";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public StreamElementsToken convert(TokenData data) {
    return new StreamElementsToken(client, data, repository, events);
  }
}
