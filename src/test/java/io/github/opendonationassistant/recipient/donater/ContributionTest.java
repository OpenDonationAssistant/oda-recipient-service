package io.github.opendonationassistant.recipient.donater;

import static org.junit.jupiter.api.Assertions.*;

import io.github.opendonationassistant.commons.Amount;
import org.junit.jupiter.api.Test;

public class ContributionTest {

  @Test
  public void testWithAmountReplacesAmountAndPreservesOtherFields() {
    var original = new Contribution(
      "id",
      "recipientId",
      "nickname",
      "2026_3",
      new Amount(1, 1, "RUB")
    );

    var updated = original.withAmount(new Amount(2, 2, "RUB"));

    assertEquals(
      new Contribution(
        "id",
        "recipientId",
        "nickname",
        "2026_3",
        new Amount(2, 2, "RUB")
      ),
      updated
    );
    assertEquals(new Amount(1, 1, "RUB"), original.amount());
  }
}
