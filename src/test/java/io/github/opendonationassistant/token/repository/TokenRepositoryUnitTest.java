package io.github.opendonationassistant.token.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.rabbit.RabbitClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

public class TokenRepositoryUnitTest {

  private final TokenDataRepository dataRepository = mock(
    TokenDataRepository.class
  );
  private final RabbitClient events = mock(RabbitClient.class);

  private final TokenData data = new TokenData(
    "id",
    "token",
    "accessToken",
    "recipientId",
    "Twitch",
    true,
    false,
    Map.of(),
    List.of()
  );

  @Test
  public void testCreateWithUnknownSystemThrows() {
    var repository = new TokenRepository(dataRepository, List.of(), events);

    assertThrows(IllegalArgumentException.class, () ->
      repository.create("token", "accessToken", "recipientId", "Unknown")
    );
  }

  @Test
  @SuppressWarnings({ "unchecked" })
  public void testCreateRoutesToMatchingProvider() {
    var provider = mock(TokenProvider.class);
    when(provider.system()).thenReturn("Fake");
    var created = mock(GenericToken.class);
    when(
      provider.create(anyString(), anyString(), anyString(), anyMap())
    ).thenReturn(CompletableFuture.completedFuture(created));
    var repository = new TokenRepository(
      dataRepository,
      List.of(provider),
      events
    );

    var result = repository
      .create("token", "accessToken", "recipientId", "Fake")
      .join();

    assertSame(created, result);
    verify(provider).create(
      anyString(),
      eq("token"),
      eq("recipientId"),
      eq(Map.of())
    );
  }

  @Test
  public void testFindByIdFallsBackToGenericTokenForUnknownSystem() {
    when(dataRepository.findById("id")).thenReturn(Optional.of(data));
    var repository = new TokenRepository(dataRepository, List.of(), events);

    var found = repository.findById("id");

    assertTrue(found.isPresent());
    assertInstanceOf(GenericToken.class, found.get());
  }

  @Test
  public void testFindByIdIsEmptyWhenTokenDoesNotExist() {
    when(dataRepository.findById("missing")).thenReturn(Optional.empty());
    var repository = new TokenRepository(dataRepository, List.of(), events);

    assertTrue(repository.findById("missing").isEmpty());
  }

  @Test
  public void testFindByRecipientIdMapsAllRows() {
    when(dataRepository.findByRecipientIdAndDeletedFalse("recipientId"))
      .thenReturn(List.of(data));
    var repository = new TokenRepository(dataRepository, List.of(), events);

    assertEquals(1, repository.findByRecipientId("recipientId").size());
  }
}
