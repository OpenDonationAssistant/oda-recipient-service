package io.github.opendonationassistant.webhook.view;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.webhook.repository.Webhook;
import io.github.opendonationassistant.webhook.repository.WebhookRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

@Controller
public class WebhookController extends BaseController {

  private final WebhookRepository repository;

  @Inject
  public WebhookController(WebhookRepository repository) {
    this.repository = repository;
  }

  @Get("/webhooks")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<List<WebhookDto>> listWebhooks(Authentication auth) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    return HttpResponse.ok(
      repository
        .findByRecipientId(ownerId.get())
        .stream()
        .map(this::asDto)
        .toList()
    );
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
  public record WebhookDto(
    String id,
    String recipientId,
    String url,
    Webhook.Type type,
    Map<String, Object> settings,
    boolean enabled
  ) {}
}
