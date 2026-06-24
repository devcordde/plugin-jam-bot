package de.chojo.pluginjam.database.repository.jam;

import de.chojo.pluginjam.database.entity.jam.Jam;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface JamRepository extends CrudRepository<Jam, Integer> {

    @Join(value = "meta", type = Join.Type.LEFT_FETCH)
    @Join(value = "time", type = Join.Type.LEFT_FETCH)
    @Join(value = "state", type = Join.Type.LEFT_FETCH)
    @Join(value = "registrations", type = Join.Type.LEFT_FETCH)
    @NonNull
    Optional<Jam> findById(@NonNull Integer id);

    @Join(value = "meta", type = Join.Type.LEFT_FETCH)
    @Join(value = "time", type = Join.Type.LEFT_FETCH)
    @Join(value = "state", type = Join.Type.LEFT_FETCH)
    @Join(value = "registrations", type = Join.Type.LEFT_FETCH)
    List<Jam> findByGuildId(Long guildId);

    @Join(value = "meta", type = Join.Type.LEFT_FETCH)
    @Join(value = "time", type = Join.Type.LEFT_FETCH)
    @Join(value = "state", type = Join.Type.LEFT_FETCH)
    @Join(value = "registrations", type = Join.Type.LEFT_FETCH)
    Optional<Jam> findByGuildIdAndStateActive(Long guildId, boolean active);

    @Join(value = "meta", type = Join.Type.LEFT_FETCH)
    @Join(value = "time", type = Join.Type.LEFT_FETCH)
    @Join(value = "state", type = Join.Type.LEFT_FETCH)
    @Join(value = "registrations", type = Join.Type.LEFT_FETCH)
    List<Jam> findByGuildIdAndStateActiveAndStateEnded(Long guildId, boolean active, boolean ended);

    @Query("""
            WITH new_jam AS (
                INSERT INTO gamejam.jam (guild_id) VALUES (:guildId) RETURNING id
            ),
            ins_meta AS (
                INSERT INTO gamejam.jam_meta (jam_id, topic, tagline) SELECT id, :topic ,:tagline FROM new_jam
            ),
            ins_time AS (
                INSERT INTO gamejam.jam_time (jam_id, zone_id, registration_start, registration_end, jam_start, jam_end)
                SELECT id, :zoneId, :registrationStart, :registrationEnd, :jamStart, :jamEnd FROM new_jam
            ),
            ins_state AS (
                INSERT INTO gamejam.jam_state (jam_id, active, voting, ended)
                SELECT id, false, false, false FROM new_jam
            )
            SELECT id FROM new_jam
            """)
    Integer createJam(Long guildId, String topic, String tagline, String zoneId,
                      LocalDateTime registrationStart, LocalDateTime registrationEnd,
                      LocalDateTime jamStart, LocalDateTime jamEnd);

    @Query("UPDATE gamejam.jam_state SET active = :active, voting = :voting, ended = :ended WHERE jam_id = :jamId")
    void updateState(Integer jamId, boolean active, boolean voting, boolean ended);

    @Query("UPDATE gamejam.jam_state SET voting = :voting WHERE jam_id = :jamId")
    void updateVoting(Integer jamId, boolean voting);

    @Query("""
            UPDATE gamejam.jam_state SET active = false, voting = false, ended = true
            WHERE jam_id IN (SELECT id FROM gamejam.jam WHERE guild_id = :guildId)
            AND active = true
            """)
    void endActiveJams(Long guildId);
}
