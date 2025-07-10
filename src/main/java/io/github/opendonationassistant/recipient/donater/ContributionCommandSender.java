package io.github.opendonationassistant.recipient.donater;

import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;

@RabbitClient("amq.topic")
public interface ContributionCommandSender {
  void internalSend(
    @Binding String binding,
    ContributionReloadCommand notification
  );

  default void send(String recipientId) {
    this.internalSend(
        "%sdonaterstoplist".formatted(recipientId),
        new ContributionReloadCommand(recipientId)
      );
  }

  @Serdeable
  public static record ContributionReloadCommand(String recipientId) {}
}
