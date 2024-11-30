package io.github.stcarolas.oda.gateway;

import java.util.Map;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class GatewayConfigRestView {
  private final Map<String, Object> config;

  public GatewayConfigRestView(Map<String, Object> config) {
    this.config = config;
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
