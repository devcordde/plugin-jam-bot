package de.chojo.pluginjam.database.repository;

import de.chojo.pluginjam.database.entity.VotingRank;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VotesRankingRepository extends CrudRepository<VotingRank, Integer> {

    @Join(value = "team", type = Join.Type.LEFT_FETCH)
    @Join(value = "team.meta", type = Join.Type.LEFT_FETCH)
    List<VotingRank> findAllByJamId(Integer jamId);
}
