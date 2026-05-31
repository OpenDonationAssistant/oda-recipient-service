package io.github.opendonationassistant.token.repository;

import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.uuid.Generators;

@Singleton
public class DonateXTokenRepository implements TokenProvider<DonateXToken, DonateXToken.Settings> {

  private static final String SYSTEM = "DonateX";
  private final TokenDataRepository repository;

  public DonateXTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  @Override
  public CompletableFuture<DonateXToken> create(
    String token,
    String recipientId,
    DonateXToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    var data = new TokenData(
      id,
      token,
      "accessToken",
      recipientId,
      SYSTEM,
      true,
      false,
      settings.asJsonMap()
    );
    repository.save(data);
    return CompletableFuture.completedFuture(convert(data));
  }

  public Optional<DonateXToken> findById(String id) {
    return repository
      .findById(id)
      .map(this::convert);
  }

  public DonateXToken convert(TokenData data) {
    return new DonateXToken(data, repository);
  }
}
