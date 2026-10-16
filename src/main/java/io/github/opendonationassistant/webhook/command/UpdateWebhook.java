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
public class UpdateWebhook extends BaseController {

  private final WebhookRepository repository;

  @Inject
  public UpdateWebhook(WebhookRepository repository) {
    this.repository = repository;
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Post("/webhooks/commands/update-webhook")
  public CompletableFuture<HttpResponse<WebhookDto>> updateWebhook(
    Authentication auth,
    @Body UpdateWebhookCommand command
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.supplyAsync(() -> {
      var existing = repository.findById(command.id());
      if (
        existing.isEmpty() ||
        !existing.get().recipientId().equals(ownerId.get())
      ) {
        return HttpResponse.<WebhookDto>notFound();
      }
      var webhook = repository.update(
        command.id(),
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
  public record UpdateWebhookCommand(
    String id,
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
