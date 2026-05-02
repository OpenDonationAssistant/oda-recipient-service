package io.github.opendonationassistant.recipient.commands;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.recipient.repository.SettingsData;
import io.github.opendonationassistant.recipient.repository.SettingsRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Controller
public class ToggleFeature extends BaseController {

  private final SettingsRepository repository;

  @Inject
  public ToggleFeature(SettingsRepository repository) {
    this.repository = repository;
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Post("/recipients/commands/toggle-feature")
  @Operation(hidden = true)
  public CompletableFuture<HttpResponse<Void>> toggleFeature(
    Authentication auth,
    @Body ToggleFeatureCommand command
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    if (!"admin".equals(ownerId.get())) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.supplyAsync(() -> {
      repository
        .get(command.recipientId())
        .setFeatureStatus(command.name(), command.status());
      return HttpResponse.ok();
    });
  }

  @Serdeable
  public record ToggleFeatureCommand(
    String recipientId,
    String name,
    SettingsData.FeatureStatus status
  ) {}
}

