package io.github.opendonationassistant.webhook.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface WebhookDataRepository
  extends CrudRepository<Webhook, String> {

  List<Webhook> findByRecipientId(String recipientId);
}
