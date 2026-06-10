package de.chojo.pluginjam.database.repository;

import de.chojo.pluginjam.database.entity.VoteEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VotesRepository extends GenericRepository<VoteEntity, Void> {

    @Query("SELECT * FROM gamejam.vote WHERE team_id = :teamId")
    List<VoteEntity> findByTeamId(Long teamId);

    @Query("INSERT INTO gamejam.vote (team_id, voter_id, points) VALUES (:teamId, :voterId, :points) ON CONFLICT (team_id, voter_id) DO UPDATE SET points = :points")
    void save(Long teamId, Long voterId, Integer points);

    @Query("DELETE FROM gamejam.vote WHERE team_id = :teamId AND voter_id = :voterId")
    void delete(Long teamId, Long voterId);
}
