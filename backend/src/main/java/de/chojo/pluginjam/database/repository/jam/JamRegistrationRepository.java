package de.chojo.pluginjam.database.repository.jam;

import de.chojo.pluginjam.database.entity.jam.JamRegistration;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface JamRegistrationRepository extends GenericRepository<JamRegistration, Long> {

    @Query("SELECT * FROM gamejam.jam_registrations WHERE jam_id = :jamId")
    List<JamRegistration> findByJamId(Integer jamId);

    @Query("INSERT INTO gamejam.jam_registrations (jam_id, user_id) VALUES (:jamId, :userId) ON CONFLICT DO NOTHING")
    void save(Integer jamId, Long userId);

    @Query("DELETE FROM gamejam.jam_registrations WHERE jam_id = :jamId AND user_id = :userId")
    void delete(Integer jamId, Long userId);
}
