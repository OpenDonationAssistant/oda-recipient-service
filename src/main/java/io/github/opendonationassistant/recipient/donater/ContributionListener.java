package io.github.opendonationassistant.recipient.donater;

import static io.github.opendonationassistant.rabbit.Queue.Payments.CONTRIBUTIONS;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
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
  public void listen(HistoryItemEvent payment) {
    log.info("Received notification", Map.of("payment", payment));
    String recipientId = payment.recipientId();
    if (payment.type() != "payment") {
      log.debug("Payment is not payment", Map.of());
      return;
    }
    String nickname = payment.nickname();
    if (nickname == null || nickname.isEmpty()) {
      log.debug("Missing nickname", Map.of());
      return;
    }
    Amount amount = payment.amount();
    if (amount == null) {
      log.debug("Missing amount", Map.of());
      return;
    }
    ZonedDateTime timestamp = payment
      .timestamp()
      .atZone(ZoneId.systemDefault());
    int month = timestamp.get(ChronoField.MONTH_OF_YEAR);
    int year = timestamp.get(ChronoField.YEAR);
    var month_key = "%d_%d".formatted(year, month);
    var year_key = "%d".formatted(year);
    log.debug("Update year", Map.of());
    updatePeriodContribution(nickname, amount, recipientId, year_key);
    log.debug("Update month", Map.of());
    updatePeriodContribution(nickname, amount, recipientId, month_key);
    log.debug("Update daily", Map.of());
    updateDaily(nickname, amount, recipientId);
    log.debug("Send reload", Map.of());
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
