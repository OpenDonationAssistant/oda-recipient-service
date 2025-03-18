package io.github.stcarolas.oda.recipient.donater;

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
        "%sdonaterstoplist".formatted(),
        new ContributionReloadCommand(recipientId)
      );
  }

  @Serdeable
  public class ContributionReloadCommand {

    public String recipientId;

    public ContributionReloadCommand(String recipientId) {
      this.recipientId = recipientId;
    }

    public String getRecipientId() {
      return recipientId;
    }
  }
}
