package de.chojo.pluginjam.database.repository;

import de.chojo.pluginjam.database.entity.VoteEntity;
import de.chojo.pluginjam.database.entity.VotingRank;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VotesRepository extends GenericRepository<Integer, Void> {

    @Query("SELECT sum(points) FROM gamejam.votes WHERE team_id = :teamId")
    int findByTeamId(int teamId);

    @Query("SELECT sum(points) FROM gamejam.votes WHERE voter_id = :voterId")
    int findByVoterId(Long voterId);

    @Query("INSERT INTO gamejam.votes (team_id, voter_id, points) VALUES (:teamId, :voterId, :points) ON CONFLICT (team_id, voter_id) DO UPDATE SET points = :points")
    void save(int teamId, Long voterId, Integer points);

    @Query("DELETE FROM gamejam.votes WHERE team_id = :teamId AND voter_id = :voterId")
    void delete(int teamId, Long voterId);
}
