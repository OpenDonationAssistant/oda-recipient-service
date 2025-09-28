package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;

@Singleton
public class TokenRepository {

  private final TokenDataRepository repository;

  @Inject
  public TokenRepository(TokenDataRepository repository) {
    this.repository = repository;
  }

  public List<Token> findByRecipientId(String recipientId) {
    return repository
      .findByRecipientId(recipientId)
      .stream()
      .map(this::convert)
      .toList();
  }

  public Token create(
    String token,
    String type,
    String recipientId,
    String system
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    var data = new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      true,
      new HashMap<>()
    );
    repository.save(data);
    return switch (system) {
      case "donatepay" -> new DonatePayToken(data, repository);
      case "donationalerts" -> new DonationAlertsToken(data, repository);
      default -> new GenericToken(data, repository);
    };
  }

  private Token convert(TokenData data) {
    return switch (data.system()) {
      case "donatepay" -> new DonatePayToken(data, repository);
      case "donationalerts" -> new DonationAlertsToken(data, repository);
      default -> new GenericToken(data, repository);
    };
  }
}
