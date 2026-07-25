package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;

@Singleton
public class TributeTokenRepository
  extends GenericTokenProvider<TributeToken, TributeToken.Settings> {

  public TributeTokenRepository(TokenDataRepository repository) {
    super(repository);
  }

  @Override
  public String system() {
    return "Tribute";
  }

  @Override
  public String getType() {
    return "accessToken";
  }

  public TributeToken convert(TokenData data) {
    return new TributeToken(data, repository);
  }
}
