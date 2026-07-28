package io.github.opendonationassistant.recipient.view;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.recipient.repository.Settings;
import io.github.opendonationassistant.recipient.repository.SettingsRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class SettingsController extends BaseController {

  private final SettingsRepository repository;

  @Inject
  public SettingsController(SettingsRepository repository) {
    this.repository = repository;
  }

  @Get("/recipients/settings")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @ApiResponse(
    responseCode = "200",
    description = "Recipient Settings with Features",
    content = @io.swagger.v3.oas.annotations.media.Content(
      schema = @io.swagger.v3.oas.annotations.media.Schema(
        implementation = SettingsDto.class
      )
    )
  )
  public CompletableFuture<HttpResponse<SettingsDto>> getSettings(
    Authentication auth
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.supplyAsync(() -> {
      final Settings settings = repository.get(ownerId.get());
      return HttpResponse.ok(asDto(ownerId, settings));
    });
  }

  private SettingsDto asDto(Optional<String> ownerId, final Settings settings) {
    return new SettingsDto(
      settings.data().id(),
      ownerId.get(),
      settings
        .data()
        .features()
        .stream()
        .map(feature ->
          new Feature(
            feature.name(),
            FeatureStatus.valueOf(feature.status().name())
          )
        )
        .toList(),
      settings
        .data()
        .logLevels()
        .stream()
        .map(level ->
          new LogLevels(level.name(), LogLevel.valueOf(level.level().name()))
        )
        .toList()
    );
  }

  @Serdeable
  public record SettingsDto(
    String id,
    String recipientId,
    List<Feature> features,
    List<LogLevels> logLevels
  ) {}

  @Serdeable
  public record Feature(String name, FeatureStatus status) {}

  @Serdeable
  public static enum FeatureStatus {
    ENABLED,
    DISABLED,
  }

  @Serdeable
  public static record LogLevels(String name, LogLevel level) {}

  @Serdeable
  public static enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    DISABLED,
  }
}
