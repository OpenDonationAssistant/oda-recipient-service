package io.github.stcarolas.oda.gateway;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface GatewayRepository
  extends CrudRepository<GatewayConfig, String> {
  Optional<GatewayConfig> find(String recipientId, String gateway);
  void delete(String id);
}
