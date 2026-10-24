package io.github.opendonationassistant.webhook.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.github.opendonationassistant.webhook.validation.WebhookUrl;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Optional;
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
    @Valid @Body UpdateWebhookCommand command
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
        return HttpResponse.<WebhookDto>unauthorized();
      }
      var current = existing.get();
      var merged = new Webhook(
        current.id(),
        current.recipientId(),
        Optional.ofNullable(command.url()).orElse(current.url()),
        Optional.ofNullable(command.type()).orElse(current.type()),
        Optional.ofNullable(command.settings()).orElse(current.settings()),
        Optional.ofNullable(command.enabled()).orElse(current.enabled()),
        current.deleted()
      );
      return repository
        .update(merged)
        .map(this::asDto)
        .orElseGet(() -> HttpResponse.<WebhookDto>unauthorized());
    });
  }

  private HttpResponse<WebhookDto> asDto(Webhook webhook) {
    return HttpResponse.ok(
      new WebhookDto(
        webhook.id(),
        webhook.recipientId(),
        webhook.url(),
        webhook.type(),
        webhook.settings(),
        webhook.enabled()
      )
    );
  }

  @Serdeable
  public record UpdateWebhookCommand(
    @NotBlank String id,
    @WebhookUrl @Nullable String url,
    @Nullable Webhook.Type type,
    @Nullable Map<String, Object> settings,
    @Nullable Boolean enabled
  ) {}

  @Serdeable
  public record WebhookDto(
    String id,
    String recipientId,
    String url,
    Webhook.Type type,
    Map<String, Object> settings,
    boolean enabled
  ) {}
}
