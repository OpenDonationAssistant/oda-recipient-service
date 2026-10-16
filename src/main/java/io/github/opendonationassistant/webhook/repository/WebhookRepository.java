package io.github.opendonationassistant.webhook.repository;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent;
import io.github.opendonationassistant.webhook.events.WebhookChangedEvent.Event;
import io.github.opendonationassistant.webhook.repository.Webhook.Type;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
    Type type,
    boolean enabled
  ) {
    var webhook = new Webhook(
      Generators.timeBasedEpochGenerator().generate().toString(),
      recipientId,
      url,
      type,
      enabled
    );
    repository.save(webhook);
    sendEvent(webhook, Event.WEBHOOK_CREATED);
    return webhook;
  }

  public Webhook update(
    String id,
    String recipientId,
    String url,
    Type type,
    boolean enabled
  ) {
    var webhook = new Webhook(id, recipientId, url, type, enabled);
    repository.update(webhook);
    sendEvent(webhook, Event.WEBHOOK_UPDATED);
    return webhook;
  }

  public Optional<Webhook> findById(String id) {
    return repository.findById(id);
  }

  public List<Webhook> findByRecipientId(String recipientId) {
    return repository.findByRecipientId(recipientId);
  }

  private void sendEvent(Webhook webhook, Event event) {
    try {
      events.sendEvent(
        new WebhookChangedEvent(
          webhook.id(),
          webhook.recipientId(),
          webhook.url(),
          webhook.type(),
          webhook.enabled(),
          event
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
