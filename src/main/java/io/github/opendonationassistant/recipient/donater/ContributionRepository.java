package io.github.opendonationassistant.recipient.donater;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ContributionRepository
  extends CrudRepository<Contribution, String> {
  List<Contribution> getByRecipientIdAndPeriod(
    String recipientId,
    String period
  );
  Optional<Contribution> getByRecipientIdAndPeriodAndNickname(
    String recipientId,
    String period,
    String nickname
  );
}
