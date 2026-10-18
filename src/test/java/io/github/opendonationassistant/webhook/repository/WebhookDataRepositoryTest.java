package io.github.opendonationassistant.webhook.repository;

import static org.junit.jupiter.api.Assertions.*;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Integration test proving the {@code String} id maps to a native Postgres {@code uuid} column
 * (via {@code @MappedProperty(type = DataType.UUID)}), that the {@code settings} map round-trips
 * through a {@code jsonb} column, and that soft-deleted rows are excluded from the active finders.
 */
@MicronautTest(environments = "allinone")
public class WebhookDataRepositoryTest {

  @Inject
  WebhookDataRepository repository;

  @Test
  public void testSaveAndFindByIdWithUuidColumn() {
    var webhook = new Webhook(
      UUID.randomUUID().toString(),
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      Map.of("channel", "general"),
      true,
      false
    );

    repository.save(webhook);

    var read = repository.findByIdAndDeletedFalse(webhook.id());
    assertTrue(read.isPresent(), "webhook should be found by its String uuid id");
    assertEquals(webhook, read.get());
  }

  @Test
  public void testSettingsRoundTripThroughJsonb() {
    var settings = Map.<String, Object>of(
      "chatId",
      "12345",
      "silent",
      "true"
    );
    var webhook = new Webhook(
      UUID.randomUUID().toString(),
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.TELEGRAM,
      settings,
      true,
      false
    );

    repository.save(webhook);

    var read = repository.findByIdAndDeletedFalse(webhook.id());
    assertTrue(read.isPresent());
    assertEquals(settings, read.get().settings());
  }

  @Test
  public void testFindByRecipientIdExcludesDeleted() {
    var recipientId = "recipient-" + UUID.randomUUID();
    var active = new Webhook(
      UUID.randomUUID().toString(),
      recipientId,
      "https://example.com/active",
      Webhook.Type.TELEGRAM,
      Map.of("chatId", "67890"),
      true,
      false
    );
    var deleted = new Webhook(
      UUID.randomUUID().toString(),
      recipientId,
      "https://example.com/deleted",
      Webhook.Type.TELEGRAM,
      Map.of(),
      true,
      true
    );

    repository.save(active);
    repository.save(deleted);

    var all = repository.findByRecipientIdAndDeletedFalse(recipientId);
    assertEquals(1, all.size());
    assertEquals(active, all.get(0));
    assertTrue(
      repository.findByIdAndDeletedFalse(deleted.id()).isEmpty(),
      "soft-deleted webhook must not be returned by the active finder"
    );
  }
}
