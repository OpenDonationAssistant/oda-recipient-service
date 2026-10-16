package io.github.opendonationassistant.webhook.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.webhook.repository.Webhook.Type;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Controller
public class CreateWebhook extends BaseController {

  private final WebhookRepository repository;

  @Inject
  public CreateWebhook(WebhookRepository repository) {
    this.repository = repository;
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Post("/webhooks/commands/create-webhook")
  public CompletableFuture<HttpResponse<WebhookDto>> createWebhook(
    Authentication auth,
    @Body CreateWebhookCommand command
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.supplyAsync(() -> {
      var webhook = repository.create(
        ownerId.get(),
        command.url(),
        command.type(),
        command.enabled()
      );
      return HttpResponse.ok(
        new WebhookDto(
          webhook.id(),
          webhook.recipientId(),
          webhook.url(),
          webhook.type(),
          webhook.enabled()
        )
      );
    });
  }

  @Serdeable
  public record CreateWebhookCommand(
    String url,
    Type type,
    boolean enabled
  ) {}

  @Serdeable
  public record WebhookDto(
    String id,
    String recipientId,
    String url,
    Type type,
    boolean enabled
  ) {}
}
