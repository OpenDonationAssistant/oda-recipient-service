package io.github.opendonationassistant.recipient.donater;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public class DonatersCacheCleaner {

  private Map<String, DailyContribution> dailyContribution;

  @Inject
  public DonatersCacheCleaner(
    Map<String, DailyContribution> dailyContribution
  ) {
    this.dailyContribution = dailyContribution;
  }

  @Scheduled(cron = "0 0 * * * ")
  public void execute() {
    dailyContribution.clear();
  }
}
