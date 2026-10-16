package io.github.opendonationassistant.recipient.donater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import org.mockito.ArgumentCaptor;

public class ContributionListenerTest {

  private static final Instant TIMESTAMP = Instant.parse(
    "2026-03-15T12:00:00Z"
  );

  private final ContributionRepository repository = mock(
    ContributionRepository.class
  );
  private final ContributionCommandSender commandSender = mock(
    ContributionCommandSender.class
  );
  private final Map<String, DailyContribution> dailyContribution =
    new HashMap<>();

  private final ContributionListener listener = new ContributionListener(
    repository,
    commandSender,
    dailyContribution
  );

  private HistoryItemEvent payment(
    String type,
    @Nullable String nickname,
    @Nullable Amount amount
  ) {
    return new HistoryItemEvent(
      "eventId",
      type,
      "recipientId",
      "twitch",
      "originId",
      TIMESTAMP,
      nickname,
      amount,
      "message",
      List.of(),
      List.of(),
      null
    );
  }

  @Test
  public void testIgnoresNonPaymentEvents() {
    listener.listen(payment("follow", "nickname", new Amount(10, 0, "RUB")));

    verifyNoInteractions(repository);
    verifyNoInteractions(commandSender);
  }

  @Test
  public void testIgnoresEventsWithoutNickname() {
    listener.listen(payment("payment", null, new Amount(10, 0, "RUB")));

    verifyNoInteractions(repository);
  }

  @Test
  public void testIgnoresEventsWithoutAmount() {
    listener.listen(payment("payment", "nickname", null));

    verifyNoInteractions(repository);
  }

  @Test
  public void testSavesNewContributionForYearAndMonthPeriods() {
    when(
      repository.getByRecipientIdAndPeriodAndNickname(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(Optional.empty());

    listener.listen(payment("payment", "nickname", new Amount(10, 5, "RUB")));

    var captor = ArgumentCaptor.forClass(Contribution.class);
    verify(repository, times(2)).save(captor.capture());
    var periods = captor
      .getAllValues()
      .stream()
      .map(Contribution::period)
      .toList();
    assertTrue(periods.contains("2026"));
    assertTrue(periods.contains("2026_3"));
    captor
      .getAllValues()
      .forEach(contribution -> {
        assertEquals("recipientId", contribution.recipientId());
        assertEquals("nickname", contribution.nickname());
        assertEquals(new Amount(10, 5, "RUB"), contribution.amount());
      });
  }

  @Test
  public void testAccumulatesExistingContribution() {
    when(
      repository.getByRecipientIdAndPeriodAndNickname(
        "recipientId",
        "2026_3",
        "nickname"
      )
    ).thenReturn(
      Optional.of(
        new Contribution(
          "contributionId",
          "recipientId",
          "nickname",
          "2026_3",
          new Amount(5, 0, "RUB")
        )
      )
    );
    when(
      repository.getByRecipientIdAndPeriodAndNickname(
        "recipientId",
        "2026",
        "nickname"
      )
    ).thenReturn(Optional.empty());

    listener.listen(payment("payment", "nickname", new Amount(10, 5, "RUB")));

    var captor = ArgumentCaptor.forClass(Contribution.class);
    verify(repository).update(captor.capture());
    assertEquals(new Amount(15, 5, "RUB"), captor.getValue().amount());
  }

  @Test
  public void testAccumulatesDailyContributionInCache() {
    when(
      repository.getByRecipientIdAndPeriodAndNickname(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(Optional.empty());

    listener.listen(payment("payment", "nickname", new Amount(10, 5, "RUB")));
    listener.listen(payment("payment", "nickname", new Amount(1, 1, "RUB")));

    var daily = dailyContribution.get("recipientId");
    assertNotNull(daily);
    assertEquals(
      new Amount(11, 6, "RUB"),
      daily.getContributions().get("nickname")
    );
  }

  @Test
  public void testSendsReloadCommandAfterProcessingPayment() {
    when(
      repository.getByRecipientIdAndPeriodAndNickname(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(Optional.empty());

    listener.listen(payment("payment", "nickname", new Amount(10, 5, "RUB")));

    verify(commandSender).send("recipientId");
  }
}
