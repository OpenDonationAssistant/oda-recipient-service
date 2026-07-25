package io.github.opendonationassistant.integration.donatepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DonatePayClient {

  private final DonatePayClientApi client;

  @Inject
  public DonatePayClient(DonatePayClientApi client) {
    this.client = client;
  }

  public CompletableFuture<UserData> getUser(String token) {
    return client.getUser(token).thenApply(wrapper -> wrapper.data());
  }

  @Client("donatepay")
  public static interface DonatePayClientApi {
    @Get("/api/v1/user")
    public CompletableFuture<ResponseWrapper<UserData>> getUser(
      @QueryValue("access_token") String token
    );
  }

  @Serdeable
  public static record ResponseWrapper<T>(String status, String time, T data) {}

  @Serdeable
  public static record UserData(
    Long id,
    String name,
    String avatar,
    Long balance,
    @JsonProperty("cashout_sum") Long cashoutSum
  ) {}
}
