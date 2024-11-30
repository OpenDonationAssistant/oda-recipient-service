package io.github.stcarolas.oda.recipient.donater;

import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RabbitListener
public class ContributionListener {

  private Logger log = LoggerFactory.getLogger(ContributionListener.class);

  private ContributionRepository repository;
  private Map<String, DailyContribution> dailyContribution;

  @Inject
  public ContributionListener(
    ContributionRepository repository,
    Map<String, DailyContribution> dailyContribution
  ) {
    this.repository = repository;
    this.dailyContribution = dailyContribution;
  }

  @Queue("payments_for_contributions")
  public void listen(CompletedPaymentNotification payment) {
    log.info("Received notification: {}", payment);
    String recipientId = payment.getRecipientId();
    String nickname = payment.getNickname();
    ZonedDateTime timestamp = payment
      .getAuthorizationTimestamp()
      .atZone(ZoneId.systemDefault());
    int month = timestamp.get(ChronoField.MONTH_OF_YEAR);
    int year = timestamp.get(ChronoField.YEAR);
    var month_key = "%d_%d".formatted(year, month);
    var year_key = "%d".formatted(year);
    Amount amount = payment.getAmount();
    updatePeriodContribution(nickname, amount, recipientId, year_key);
    updatePeriodContribution(nickname, amount, recipientId, month_key);
    updateDaily(nickname, amount, recipientId);
  }

  private void updateDaily(String nickname, Amount amount, String recipientId) {
    Map<String, Amount> daily = dailyContribution
      .getOrDefault(recipientId, new DailyContribution())
      .getContributions();
    Amount result = Optional
      .ofNullable(daily.get(nickname))
      .map(it ->
        new Amount(
          it.getMajor() + amount.getMajor(),
          it.getMinor() + amount.getMinor(),
          it.getCurrency()
        )
      )
      .orElseGet(() -> amount);
    daily.put(nickname, result);
    dailyContribution.put(recipientId, new DailyContribution(daily));
  }

  private void updatePeriodContribution(
    String nickname,
    Amount amount,
    String recipientId,
    String period
  ) {
    repository
      .getByRecipientIdAndPeriodAndNickname(recipientId, period, nickname)
      .map(contribution -> {
        Amount oldAmount = contribution.getAmount();
        var newAmount = new Amount(
          oldAmount.getMajor() + amount.getMajor(),
          oldAmount.getMinor() + amount.getMinor(),
          amount.getCurrency()
        );
        contribution.setAmount(newAmount);
        return contribution;
      })
      .ifPresentOrElse(
        repository::update,
        () -> {
          var contribution = new Contribution();
          contribution.setId(UUID.randomUUID().toString());
          contribution.setAmount(amount);
          contribution.setPeriod(period);
          contribution.setNickname(nickname);
          contribution.setRecipientId(recipientId);
          repository.save(contribution);
        }
      );
  }
}
