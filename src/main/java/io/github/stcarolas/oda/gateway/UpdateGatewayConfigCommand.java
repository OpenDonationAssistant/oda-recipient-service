package io.github.stcarolas.oda.gateway;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
public class UpdateGatewayConfigCommand {

  private final String gateway;
  private final Map<String, Object> config;

  public UpdateGatewayConfigCommand(
    @NonNull Map<String, Object> config,
    String gateway
  ) {
    this.config = config;
    this.gateway = gateway;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void execute(String recipientId) {
    var newConfig = new GatewayConfig();
    newConfig.setId(recipientId.concat(gateway));
    newConfig.setGateway(gateway);
    newConfig.setRecipientId(recipientId);
    newConfig.setConfig(config);
    newConfig.save();
  }
}
