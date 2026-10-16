package io.github.opendonationassistant.webhook.repository;

import static org.junit.jupiter.api.Assertions.*;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Integration test proving the {@code String} id maps to a native Postgres {@code uuid} column
 * (via {@code @MappedProperty(type = DataType.UUID)}). Exercises the real Micronaut Data JDBC
 * binder/reader, which the pure Mockito tests cannot.
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
      true
    );

    repository.save(webhook);

    var read = repository.findById(webhook.id());
    assertTrue(read.isPresent(), "webhook should be found by its String uuid id");
    assertEquals(webhook, read.get());
  }

  @Test
  public void testFindByRecipientId() {
    var recipientId = "recipient-" + UUID.randomUUID();
    var webhook = new Webhook(
      UUID.randomUUID().toString(),
      recipientId,
      "https://example.com/hook",
      Webhook.Type.TELEGRAM,
      false
    );

    repository.save(webhook);

    var all = repository.findByRecipientId(recipientId);
    assertEquals(1, all.size());
    assertEquals(webhook, all.get(0));
  }

  @Test
  public void testDeleteRemovesWebhook() {
    var webhook = new Webhook(
      UUID.randomUUID().toString(),
      "recipientId",
      "https://example.com/hook",
      Webhook.Type.DISCORD,
      true
    );

    repository.save(webhook);
    assertTrue(repository.findById(webhook.id()).isPresent());

    repository.deleteById(webhook.id());

    assertFalse(
      repository.findById(webhook.id()).isPresent(),
      "webhook should be gone after deleteById"
    );
  }
}
