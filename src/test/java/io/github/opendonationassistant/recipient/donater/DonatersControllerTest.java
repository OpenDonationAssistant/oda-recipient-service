package io.github.opendonationassistant.recipient.donater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.testutils.AuthenticationGenerator;
import io.micronaut.security.authentication.Authentication;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DonatersControllerTest {

  private final ContributionRepository repository = mock(
    ContributionRepository.class
  );
  private final Map<String, DailyContribution> dailyContribution =
    new HashMap<>();

  private final DonatersController controller = new DonatersController(
    repository,
    dailyContribution
  );

  @Test
  public void testReturnsDailyContributionsFromCache() {
    dailyContribution.put(
      "recipientId",
      new DailyContribution(Map.of("nickname", new Amount(10, 0, "RUB")))
    );

    var response = controller.list(AuthenticationGenerator.forUser("recipientId"), "recipientId", "day");

    assertEquals(200, response.getStatus().getCode());
    var body = response.body();
    assertNotNull(body);
    assertEquals(new Amount(10, 0, "RUB"), body.get("nickname"));
    verifyNoInteractions(repository);
  }

  @Test
  public void testReturnsEmptyMapForDayWhenCacheIsEmpty() {
    var response = controller.list(AuthenticationGenerator.forUser("recipientId"), "recipientId", "day");

    var body = response.body();
    assertNotNull(body);
    assertTrue(body.isEmpty());
  }

  @Test
  public void testQueriesDatabaseWithYearMonthKeyForMonthPeriod() {
    var expectedKey = "%d_%d".formatted(
      ZonedDateTime.now().get(ChronoField.YEAR),
      ZonedDateTime.now().get(ChronoField.MONTH_OF_YEAR)
    );
    when(repository.getByRecipientIdAndPeriod("recipientId", expectedKey))
      .thenReturn(
        new ArrayList<>(
          List.of(
            new Contribution(
              "id",
              "recipientId",
              "nickname",
              expectedKey,
              new Amount(3, 0, "RUB")
            )
          )
        )
      );

    var response = controller.list(
      AuthenticationGenerator.forUser("recipientId"),
      "recipientId",
      "month"
    );

    var body = response.body();
    assertNotNull(body);
    assertEquals(new Amount(3, 0, "RUB"), body.get("nickname"));
    verify(repository).getByRecipientIdAndPeriod("recipientId", expectedKey);
  }

  @Test
  public void testQueriesDatabaseWithYearKeyForYearPeriod() {
    var expectedKey = "%d".formatted(
      ZonedDateTime.now().get(ChronoField.YEAR)
    );
    when(repository.getByRecipientIdAndPeriod("recipientId", expectedKey))
      .thenReturn(new ArrayList<>());

    var response = controller.list(AuthenticationGenerator.forUser("recipientId"), "recipientId", "year");

    var body = response.body();
    assertNotNull(body);
    assertTrue(body.isEmpty());
    verify(repository).getByRecipientIdAndPeriod("recipientId", expectedKey);
  }

  @Test
  public void testRejectsAnotherRecipientsData() {
    var response = controller.list(
      AuthenticationGenerator.forUser("anotherRecipient"),
      "recipientId",
      "day"
    );

    assertEquals(403, response.getStatus().getCode());
    verifyNoInteractions(repository);
  }

  @Test
  public void testRejectsRequestWithoutOwner() {
    var auth = mock(Authentication.class);
    when(auth.getAttributes()).thenReturn(Map.of());

    var response = controller.list(auth, "recipientId", "day");

    assertEquals(401, response.getStatus().getCode());
    verifyNoInteractions(repository);
  }
}
