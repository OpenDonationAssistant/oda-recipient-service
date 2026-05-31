package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class DonatePayTokenRepository implements TokenProvider<DonatePayToken> {

  private static final String SYSTEM = "DonatePay";
  private final TokenDataRepository repository;

  public DonatePayTokenRepository(TokenDataRepository repository) {
    this.repository = repository;
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
  public DonatePayToken create(
    String token,
    String recipientId,
    Map<String, Object> settings
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
      settings
    );
    repository.save(data);
    return convert(data);
  }
}
