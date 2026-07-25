package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DonationAlertsTokenRepository
  implements TokenProvider<DonationAlertsToken, DonationAlertsToken.Settings> {

  private static final String SYSTEM = "DonationAlerts";
  private final TokenDataRepository repository;

  public DonationAlertsTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  @Override
  public CompletableFuture<DonationAlertsToken> create(
    String token,
    String recipientId,
    DonationAlertsToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<DonationAlertsToken> create(
    String id,
    String token,
    String recipientId,
    Map<String, Object> settings
  ) {
    var data = new TokenData(
      id,
      token,
      "accessToken",
      recipientId,
      SYSTEM,
      true,
      false,
      settings
    );
    repository.save(data);
    return CompletableFuture.completedFuture(convert(data));
  }

  public Optional<DonationAlertsToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public DonationAlertsToken convert(TokenData data) {
    return new DonationAlertsToken(data, repository);
  }
}
