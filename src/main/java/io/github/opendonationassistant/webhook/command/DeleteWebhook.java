package io.github.opendonationassistant.webhook.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
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
public class DeleteWebhook extends BaseController {

  private final WebhookRepository repository;

  @Inject
  public DeleteWebhook(WebhookRepository repository) {
    this.repository = repository;
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Post("/webhooks/commands/delete-webhook")
  public CompletableFuture<HttpResponse<Void>> deleteWebhook(
    Authentication auth,
    @Body DeleteWebhookCommand command
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
        return HttpResponse.<Void>notFound();
      }
      repository.delete(existing.get());
      return HttpResponse.<Void>ok();
    });
  }

  @Serdeable
  public record DeleteWebhookCommand(String id) {}
}
