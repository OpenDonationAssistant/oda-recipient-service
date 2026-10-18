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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    @Valid @Body DeleteWebhookCommand command
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
        return HttpResponse.<Void>unauthorized();
      }
      return repository.delete(existing.get())
        ? HttpResponse.<Void>ok()
        : HttpResponse.<Void>unauthorized();
    });
  }

  @Serdeable
  public record DeleteWebhookCommand(@NotBlank String id) {}
}
