package io.github.stcarolas.oda.recipient.donater;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DonatersCacheCleaner {

  private Logger log = LoggerFactory.getLogger(DonatersCacheCleaner.class);

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
