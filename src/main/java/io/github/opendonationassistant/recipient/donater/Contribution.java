package io.github.opendonationassistant.recipient.donater;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("contribution")
public record Contribution(
  @Id String id,
  @MappedProperty("recipient_id") String recipientId,
  String nickname,
  String period,
  Amount amount
) {
  public Contribution withAmount(Amount amount) {
    return new Contribution(id, recipientId, nickname, period, amount);
  }
}
