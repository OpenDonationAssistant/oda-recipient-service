package io.github.opendonationassistant.recipient.donater;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/recipients/{recipientId}/donaters")
public class DonatersController {

  private final ContributionRepository repository;
  private Map<String, DailyContribution> dailyContribution;

  @Inject
  public DonatersController(
    ContributionRepository repository,
    Map<String, DailyContribution> dailyContribution
  ) {
    this.repository = repository;
    this.dailyContribution = dailyContribution;
  }

  @Get
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<Map<String, Amount>> list(
    @PathVariable("recipientId") String recipientId,
    @QueryValue("period") String period
  ) {
    if ("day".equalsIgnoreCase(period)) {
      return HttpResponse.ok(
        dailyContribution
          .getOrDefault(recipientId, new DailyContribution())
          .getContributions()
      );
    }
    return HttpResponse.ok(donatersFromDb(recipientId, period));
  }

  private Map<String, Amount> donatersFromDb(
    String recipientId,
    String period
  ) {
    ZonedDateTime now = ZonedDateTime.now();
    String periodKey = "";
    if ("month".equalsIgnoreCase(period)) {
      periodKey = "%d_%d".formatted(
          now.get(ChronoField.YEAR),
          now.get(ChronoField.MONTH_OF_YEAR)
        );
    }
    if ("year".equalsIgnoreCase(period)) {
      periodKey = "%d".formatted(now.get(ChronoField.YEAR));
    }
    List<Contribution> contributions = repository.getByRecipientIdAndPeriod(
      recipientId,
      periodKey
    );
    contributions.sort((a, b) -> {
      return a.amount().getMajor() - b.amount().getMajor();
    });
    Map<String, Amount> donaters = new HashMap<>();
    contributions.forEach(contribution -> {
      donaters.put(contribution.nickname(), contribution.amount());
    });
    return donaters;
  }
}
