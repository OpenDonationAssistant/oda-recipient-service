package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.integration.donatepay.DonatePayClient;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DonatePayTokenRepository
  implements TokenProvider<DonatePayToken, DonatePayToken.Settings> {

  private static final String SYSTEM = "DonatePay";
  private final TokenDataRepository repository;
  private final DonatePayClient client;

  public DonatePayTokenRepository(
    TokenDataRepository repository,
    DonatePayClient client
  ) {
    this.repository = repository;
    this.client = client;
  }

  @Override
  public String system() {
    return SYSTEM;
  }

  public Optional<DonatePayToken> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public DonatePayToken convert(TokenData data) {
    return new DonatePayToken(data, repository);
  }

  @Override
  public CompletableFuture<DonatePayToken> create(
    String token,
    String recipientId,
    DonatePayToken.Settings settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<DonatePayToken> create(
    String id,
    String token,
    String recipientId,
    Map<String, Object> settings
  ) {
    return client
      .getUser(token)
      .thenApply(user -> {
        var fullSettings = new HashMap<>(settings);
        fullSettings.put("id", user.id());
        var data = new TokenData(
          id,
          token,
          "accessToken",
          recipientId,
          SYSTEM,
          true,
          false,
          fullSettings
        );
        var created = convert(data);
        created.save();
        return created;
      });
  }
}
