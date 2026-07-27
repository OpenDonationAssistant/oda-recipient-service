package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.streamlabs.StreamlabsClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class StreamlabsTokenRepository
  extends GenericTokenProvider<StreamlabsToken, StreamlabsToken.Settings> {

  private final RabbitClient events;
  private final StreamlabsClient client;

  @Inject
  public StreamlabsTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    StreamlabsClient client
  ) {
    super(repository);
    this.events = events;
    this.client = client;
  }

  @Override
  public String system() {
    return "Streamlabs";
  }

  @Override
  public String getType() {
    return "refreshToken";
  }

  public StreamlabsToken convert(TokenData data) {
    return new StreamlabsToken(client, data, repository, events);
  }
}
