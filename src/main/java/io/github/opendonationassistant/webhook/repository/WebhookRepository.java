package io.github.opendonationassistant.webhook.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class WebhookRepository {

  private final WebhookDataRepository repository;
  private final RabbitClient events;

  @Inject
  public WebhookRepository(
    WebhookDataRepository repository,
    @Named("events") RabbitClient events
  ) {
    this.repository = repository;
    this.events = events;
  }

  public Webhook create(
    String recipientId,
    String url,
    Webhook.Type type,
    Map<String, Object> settings,
    boolean enabled
  ) {
    var webhook = new Webhook(
      Generators.timeBasedEpochGenerator().generate().toString(),
      recipientId,
      url,
      type,
      settings,
      enabled,
      false
    );
    repository.save(webhook);
    sendEvent(webhook, Event.WEBHOOK_CREATED);
    return webhook;
  }

  public Optional<Webhook> update(Webhook webhook) {
    if (
      !repository.existsByIdAndRecipientIdAndDeletedFalse(
        webhook.id(),
        webhook.recipientId()
      )
    ) {
      return Optional.empty();
    }
    repository.update(webhook);
    sendEvent(webhook, Event.WEBHOOK_UPDATED);
    return Optional.of(webhook);
  }

  /**
   * Soft-deletes an active webhook owned by the recipient. Returns false (and emits no event) when
   * there is nothing to delete.
   */
  public boolean delete(Webhook webhook) {
    if (
      !repository.existsByIdAndRecipientIdAndDeletedFalse(
        webhook.id(),
        webhook.recipientId()
      )
    ) {
      return false;
    }
    var deleted = webhook.withDeleted(true);
    repository.update(deleted);
    sendEvent(deleted, Event.WEBHOOK_DELETED);
    return true;
  }

  public Optional<Webhook> findById(String id) {
    if (!isUuid(id)) {
      return Optional.empty();
    }
    return repository.findByIdAndDeletedFalse(id);
  }

  public List<Webhook> findByRecipientId(String recipientId) {
    return repository.findByRecipientIdAndDeletedFalse(recipientId);
  }

  private static boolean isUuid(String id) {
    if (id == null) {
      return false;
    }
    try {
      UUID.fromString(id);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private void sendEvent(Webhook webhook, Event event) {
    try {
      events.sendEvent(
        new WebhookChangedEvent(
          webhook.id(),
          webhook.recipientId(),
          webhook.url(),
          webhook.type(),
          webhook.settings(),
          webhook.enabled(),
          event
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
