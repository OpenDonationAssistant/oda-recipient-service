package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class StreamElementsJWTTokenRepository
  extends GenericTokenProvider<
    StreamElementsJWTToken, StreamElementsToken.Settings
  > {

  private final RabbitClient events;

  @Inject
  public StreamElementsJWTTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    super(repository);
    this.events = events;
  }

  @Override
  public String system() {
    return "StreamElementsJWT";
  }

  @Override
  public StreamElementsJWTToken convert(TokenData data) {
    return new StreamElementsJWTToken(data, repository, events);
  }

  @Override
  public String getType() {
    return "accessToken";
  }
}
