package io.github.opendonationassistant.token.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface TokenDataRepository extends CrudRepository<TokenData, String> {
  public List<TokenData> findByRecipientId(String recipientId);
}
