package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class GoogleApiKeyTokenRepository
  extends GenericTokenProvider<
    GoogleApiKeyToken,
    GoogleApiKeyToken.Settings
  > {

  private final RabbitClient events;

  @Inject
  public GoogleApiKeyTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    super(repository);
    this.events = events;
  }

  @Override
  public String system() {
    return "GoogleApiKey";
  }

  @Override
  public GoogleApiKeyToken convert(TokenData data) {
    return new GoogleApiKeyToken(data, repository, events);
  }

  @Override
  public String getType() {
    return "accessToken";
  }
}