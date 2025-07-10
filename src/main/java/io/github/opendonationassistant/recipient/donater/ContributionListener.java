package io.github.opendonationassistant.recipient.donater;

import static io.github.opendonationassistant.rabbit.Queue.Payments.CONTRIBUTIONS;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.micronaut.core.util.StringUtils;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RabbitListener
public class ContributionListener {

  private ODALogger log = new ODALogger(this);

  private ContributionRepository repository;
  private ContributionCommandSender commandSender;
  private Map<String, DailyContribution> dailyContribution;

  @Inject
  public ContributionListener(
    ContributionRepository repository,
    ContributionCommandSender commandSender,
    Map<String, DailyContribution> dailyContribution
  ) {
    this.repository = repository;
    this.commandSender = commandSender;
    this.dailyContribution = dailyContribution;
  }

  @Queue(CONTRIBUTIONS)
  public void listen(CompletedPaymentNotification payment) {
    log.info("Received notification", Map.of("payment", payment));
    String recipientId = payment.recipientId();
    String nickname = payment.nickname();
    if (StringUtils.isEmpty(nickname)) {
      return;
    }
    ZonedDateTime timestamp = payment
      .authorizationTimestamp()
      .atZone(ZoneId.systemDefault());
    int month = timestamp.get(ChronoField.MONTH_OF_YEAR);
    int year = timestamp.get(ChronoField.YEAR);
    var month_key = "%d_%d".formatted(year, month);
    var year_key = "%d".formatted(year);
    Amount amount = payment.amount();
    updatePeriodContribution(nickname, amount, recipientId, year_key);
    updatePeriodContribution(nickname, amount, recipientId, month_key);
    updateDaily(nickname, amount, recipientId);
    commandSender.send(recipientId);
  }

  private void updateDaily(String nickname, Amount amount, String recipientId) {
    Map<String, Amount> daily = dailyContribution
      .getOrDefault(recipientId, new DailyContribution())
      .getContributions();
    Amount result = Optional.ofNullable(daily.get(nickname))
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
        Amount oldAmount = contribution.amount();
        var newAmount = new Amount(
          oldAmount.getMajor() + amount.getMajor(),
          oldAmount.getMinor() + amount.getMinor(),
          amount.getCurrency()
        );
        return contribution.withAmount(newAmount);
      })
      .ifPresentOrElse(repository::update, () ->
        repository.save(
          new Contribution(
            UUID.randomUUID().toString(),
            recipientId,
            nickname,
            period,
            amount
          )
        )
      );
  }
}
