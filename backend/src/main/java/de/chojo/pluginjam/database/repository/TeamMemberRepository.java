package de.chojo.pluginjam.database.repository;

import de.chojo.pluginjam.database.entity.team.TeamMember;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface TeamMemberRepository extends CrudRepository<TeamMember, Long> {

    void deleteByTeamIdAndUserId(Integer teamId, Long userId);
    void deleteAllByTeamId(Integer teamId);
}
