package io.github.opendonationassistant.token.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.JsonConvertable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class GenericTokenProvider<
  T extends GenericToken, S extends JsonConvertable
> implements TokenProvider<T, S> {

  protected final TokenDataRepository repository;

  public GenericTokenProvider(TokenDataRepository repository) {
    this.repository = repository;
  }

  @Override
  public CompletableFuture<T> create(
    String token,
    String recipientId,
    S settings
  ) {
    var id = Generators.timeBasedEpochGenerator().generate().toString();
    return create(id, token, recipientId, settings.asJsonMap());
  }

  @Override
  public CompletableFuture<T> create(
    String id,
    String token,
    String recipientId,
    Map<String, Object> settings
  ) {
    var data = new TokenData(
      id,
      token,
      getType(),
      recipientId,
      system(),
      true,
      false,
      settings
    );
    var created = convert(data);
    created.save();
    return CompletableFuture.completedFuture(created);
  }

  public Optional<T> findById(String id) {
    return repository.findById(id).map(this::convert);
  }

  public abstract String getType();
}
