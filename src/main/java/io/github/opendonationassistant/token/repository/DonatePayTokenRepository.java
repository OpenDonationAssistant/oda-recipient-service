package io.github.opendonationassistant.token.repository;

import io.github.opendonationassistant.integration.donatepay.DonatePayClient;
import io.github.opendonationassistant.rabbit.RabbitClient;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DonatePayTokenRepository
  extends GenericTokenProvider<DonatePayToken, DonatePayToken.Settings> {

  private final RabbitClient events;
  private final DonatePayClient client;

  public DonatePayTokenRepository(
    TokenDataRepository repository,
    @Named("events") RabbitClient events,
    DonatePayClient client
  ) {
    super(repository);
    this.events = events;
    this.client = client;
  }

  @Override
  public String system() {
    return "DonatePay";
  }

  @Override
  public String getType() {
    return "accessToken";
  }

  public DonatePayToken convert(TokenData data) {
    return new DonatePayToken(data, repository, events);
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
      .thenCompose(user -> {
        var fullSettings = new HashMap<>(settings);
        fullSettings.put("id", user.id());
        return super.create(id, token, recipientId, fullSettings);
      });
  }
}
