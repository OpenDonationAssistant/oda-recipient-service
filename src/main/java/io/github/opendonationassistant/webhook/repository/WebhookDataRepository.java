package io.github.opendonationassistant.webhook.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface WebhookDataRepository
  extends CrudRepository<Webhook, String> {

  Optional<Webhook> findByIdAndDeletedFalse(String id);

  List<Webhook> findByRecipientIdAndDeletedFalse(String recipientId);

  boolean existsByIdAndRecipientIdAndDeletedFalse(
    String id,
    String recipientId
  );
}
