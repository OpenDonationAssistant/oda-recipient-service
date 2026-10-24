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
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
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
    @Valid @Body CreateWebhookCommand command
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.supplyAsync(() -> {
      var settings = Optional.ofNullable(command.settings()).orElseGet(Map::of);
      var enabled = Optional.ofNullable(command.enabled()).orElse(true);
      var webhook = repository.create(
        ownerId.get(),
        command.url(),
        command.type(),
        settings,
        enabled
      );
      return HttpResponse.ok(asDto(webhook));
    });
  }

  private WebhookDto asDto(Webhook webhook) {
    return new WebhookDto(
      webhook.id(),
      webhook.recipientId(),
      webhook.url(),
      webhook.type(),
      webhook.settings(),
      webhook.enabled()
    );
  }

  @Serdeable
  public record CreateWebhookCommand(
    @NotBlank @WebhookUrl String url,
    @NotNull Webhook.Type type,
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
