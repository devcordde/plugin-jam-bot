package de.chojo.pluginjam.database.repository;

import de.chojo.pluginjam.database.entity.team.Team;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
@Join(value = "meta", type = Join.Type.LEFT_FETCH)
@Join(value = "members", type = Join.Type.LEFT_FETCH)
public interface TeamRepository extends CrudRepository<Team, Integer> {

    List<Team> findByJamId(Integer jamId);

    Optional<Team> findByMembersUserId(Long userId);
}
