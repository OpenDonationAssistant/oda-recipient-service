package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class TokenRepository {

  private final TokenDataRepository repository;
  private final List<TokenProvider> providers;
  private final RabbitClient events;

  @Inject
  public TokenRepository(
    TokenDataRepository repository,
    List<TokenProvider> providers,
    @Named("events") RabbitClient events
  ) {
    this.repository = repository;
    this.providers = providers;
    this.events = events;
  }

  public Optional<Token> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public List<Token> findByRecipientId(String recipientId) {
    return repository
      .findByRecipientIdAndDeletedFalse(recipientId)
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
      .findByRecipientIdAndSystemAndTypeAndDeletedFalse(
        recipientId,
        system,
        type
      )
      .stream()
      .map(this::convert)
      .toList();
  }

  public CompletableFuture<Token> create(
    String token,
    String type,
    String recipientId,
    String system
  ) {
    return create(token, type, recipientId, system, Map.of());
  }

  public CompletableFuture<Token> create(
    String token,
    String type,
    String recipientId,
    String system,
    Map<String, Object> settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, type, recipientId, system, settings);
  }

  public CompletableFuture<Token> create(
    String id,
    String token,
    String type,
    String recipientId,
    String system,
    Map<String, Object> settings
  ) {
    return providers
      .stream()
      .filter(provider -> provider.system().equals(system))
      .findFirst()
      .map(provider -> provider.create(id, token, recipientId, settings))
      .orElseThrow(() ->
        new IllegalArgumentException("Unknown system: " + system)
      );
  }

  private Token convert(TokenData data) {
    return providers
      .stream()
      .filter(provider -> provider.system().equals(data.system()))
      .findFirst()
      .map(provider -> provider.convert(data))
      .orElseGet(() -> new GenericToken(data, repository, events));
  }
}
