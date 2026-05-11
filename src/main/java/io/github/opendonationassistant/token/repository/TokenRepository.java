package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class TokenRepository {

  private final TokenDataRepository repository;
  private final List<TokenProvider> providers;

  @Inject
  public TokenRepository(
    TokenDataRepository repository,
    List<TokenProvider> providers
  ) {
    this.repository = repository;
    this.providers = providers;
  }

  public Optional<Token> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public List<Token> findByRecipientId(String recipientId) {
    return repository
      .findByRecipientId(recipientId)
      .stream()
      .map(this::convert)
      .toList();
  }

  public List<Token> findByRecipientIdAndSystemAndType(
    String recipientId,
    String system,
    String type
  ) {
    return repository
      .findByRecipientIdAndSystemAndType(recipientId, system, type)
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
    return create(token, type, recipientId, system, new HashMap<>());
  }

  public Token create(
    String token,
    String type,
    String recipientId,
    String system,
    Map<String, Object> settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    var data = new TokenData(
      id,
      token,
      type,
      recipientId,
      system,
      true,
      settings
    );
    repository.save(data);
    return convert(data);
  }

  private Token convert(TokenData data) {
    return providers
      .stream()
      .filter(provider -> provider.system().equals(data.system()))
      .findFirst()
      .map(provider -> provider.convert(data))
      .orElseGet(() -> new GenericToken(data, repository));
  }
}
