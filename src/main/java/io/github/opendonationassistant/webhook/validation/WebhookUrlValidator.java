package io.github.opendonationassistant.webhook.validation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

@Singleton
@Introspected
public class WebhookUrlValidator
  implements ConstraintValidator<WebhookUrl, String> {

  static final int MAX_LENGTH = 2048;

  private static final Set<String> BLOCKED_HOSTS = Set.of(
    "localhost",
    "localhost.localdomain",
    "ip6-localhost",
    "metadata.google.internal"
  );

  private static final Pattern NUMERIC_HOST = Pattern.compile(
    "\\d{1,3}(\\.\\d{1,3}){0,3}"
  );

  @Override
  public boolean isValid(
    @Nullable String value,
    AnnotationValue<WebhookUrl> annotation,
    ConstraintValidatorContext context
  ) {
    if (value == null) {
      return true;
    }
    if (value.length() > MAX_LENGTH) {
      return false;
    }
    final URI uri;
    try {
      uri = new URI(value);
    } catch (URISyntaxException e) {
      return false;
    }
    var scheme = uri.getScheme();
    if (scheme == null) {
      return false;
    }
    var normalizedScheme = scheme.toLowerCase(Locale.ROOT);
    if (!normalizedScheme.equals("http") && !normalizedScheme.equals("https")) {
      return false;
    }
    var host = uri.getHost();
    if (host == null || host.isBlank()) {
      return false;
    }
    return isPublicHost(host);
  }

  private static boolean isPublicHost(String host) {
    var normalized = normalizeHost(host);
    if (BLOCKED_HOSTS.contains(normalized) || normalized.endsWith(".localhost")) {
      return false;
    }
    return !isBlockedIpv4(normalized) && !isBlockedIpv6(normalized);
  }

  private static String normalizeHost(String host) {
    var normalized = host.toLowerCase(Locale.ROOT);
    if (normalized.startsWith("[") && normalized.endsWith("]")) {
      return normalized.substring(1, normalized.length() - 1);
    }
    return normalized;
  }

  private static boolean isBlockedIpv4(String host) {
    if (!NUMERIC_HOST.matcher(host).matches()) {
      return false;
    }
    var parts = host.split("\\.");
    if (parts.length < 4) {
      // Numeric shorthand such as 127.1 or 2130706433 is never a public hostname.
      return true;
    }
    var first = Integer.parseInt(parts[0]);
    var second = Integer.parseInt(parts[1]);
    return (
      first == 0 ||
      first == 10 ||
      first == 127 ||
      first >= 224 ||
      (first == 100 && second >= 64 && second <= 127) ||
      (first == 169 && second == 254) ||
      (first == 172 && second >= 16 && second <= 31) ||
      (first == 192 && second == 168) ||
      (first == 198 && (second == 18 || second == 19))
    );
  }

  private static boolean isBlockedIpv6(String host) {
    if (!host.contains(":")) {
      return false;
    }
    if (host.equals("::") || host.equals("::1")) {
      return true;
    }
    if (host.startsWith("::ffff:")) {
      return isBlockedIpv4(host.substring("::ffff:".length()));
    }
    return (
      host.startsWith("fc") ||
      host.startsWith("fd") ||
      host.startsWith("fe80")
    );
  }
}
