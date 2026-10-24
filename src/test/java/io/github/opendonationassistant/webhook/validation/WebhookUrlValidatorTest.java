package io.github.opendonationassistant.webhook.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

public class WebhookUrlValidatorTest {

  private final WebhookUrlValidator validator = new WebhookUrlValidator();
  private final AnnotationValue<WebhookUrl> annotation = AnnotationValue
    .builder(WebhookUrl.class)
    .build();
  private final ConstraintValidatorContext context = mock(
    ConstraintValidatorContext.class
  );

  @Test
  public void testAcceptsPublicHttpsUrl() {
    assertTrue(isValid("https://example.com/hook"));
  }

  @Test
  public void testAcceptsHttpUrlWithPortPathAndQuery() {
    assertTrue(isValid("http://example.com:8080/hook?channel=general"));
  }

  @Test
  public void testAcceptsPublicIpv4Address() {
    assertTrue(isValid("https://8.8.8.8/hook"));
  }

  @Test
  public void testAcceptsNullBecauseNullabilityIsValidatedElsewhere() {
    assertTrue(validator.isValid(null, annotation, context));
  }

  @Test
  public void testRejectsRelativeAndMalformedUrls() {
    assertFalse(isValid("example.com/hook"));
    assertFalse(isValid("/hook"));
    assertFalse(isValid("   "));
    assertFalse(isValid("not a url"));
  }

  @Test
  public void testRejectsNonHttpSchemes() {
    assertFalse(isValid("ftp://example.com/hook"));
    assertFalse(isValid("file:///etc/passwd"));
    assertFalse(isValid("javascript:alert(1)"));
    assertFalse(isValid("gopher://example.com/hook"));
  }

  @Test
  public void testRejectsLocalhostFamily() {
    assertFalse(isValid("http://localhost:8080/hook"));
    assertFalse(isValid("http://LOCALHOST/hook"));
    assertFalse(isValid("http://api.localhost/hook"));
    assertFalse(isValid("http://metadata.google.internal/hook"));
  }

  @Test
  public void testRejectsLoopbackAndPrivateIpv4Literals() {
    assertFalse(isValid("http://127.0.0.1/hook"));
    assertFalse(isValid("http://127.1/hook"));
    assertFalse(isValid("http://10.0.0.5/hook"));
    assertFalse(isValid("http://172.16.0.1/hook"));
    assertFalse(isValid("http://172.31.255.255/hook"));
    assertFalse(isValid("http://192.168.1.10/hook"));
    assertFalse(isValid("http://169.254.169.254/latest/meta-data"));
    assertFalse(isValid("http://0.0.0.0/hook"));
    assertFalse(isValid("http://100.64.0.1/hook"));
    assertFalse(isValid("http://198.18.0.1/hook"));
    assertFalse(isValid("http://224.0.0.1/hook"));
  }

  @Test
  public void testRejectsPrivateIpv6Literals() {
    assertFalse(isValid("http://[::1]/hook"));
    assertFalse(isValid("http://[::]/hook"));
    assertFalse(isValid("http://[fd00::1]/hook"));
    assertFalse(isValid("http://[fe80::1]/hook"));
    assertFalse(isValid("http://[::ffff:127.0.0.1]/hook"));
  }

  @Test
  public void testAllowsPublicIpv6Literal() {
    assertTrue(isValid("http://[2001:4860:4860::8888]/hook"));
  }

  @Test
  public void testRejectsUrlsLongerThanTheLimit() {
    var prefix = "https://example.com/";
    var url = prefix + "a".repeat(WebhookUrlValidator.MAX_LENGTH);

    assertFalse(isValid(url));
  }

  private boolean isValid(String value) {
    return validator.isValid(value, annotation, context);
  }
}
