package io.github.stcarolas.oda.gateway;

import io.github.stcarolas.oda.Beans;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("gateway_config")
public class GatewayConfig {

  @Id
  private String id;

  @MappedProperty(value = "recipient_id")
  private String recipientId;

  @MappedProperty(value = "gateway")
  private String gateway;

  @MappedProperty(type = DataType.JSON)
  private java.util.Map<String, Object> config;

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public String getGateway() {
    return gateway;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public java.util.Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(java.util.Map<String, Object> config) {
    this.config = config;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void save() {
    Beans.get(GatewayRepository.class).save(this);
  }
}
