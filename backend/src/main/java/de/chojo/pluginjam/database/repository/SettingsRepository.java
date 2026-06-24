package de.chojo.pluginjam.database.repository;

import de.chojo.pluginjam.database.entity.SettingsEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface SettingsRepository extends GenericRepository<SettingsEntity, Long> {

    @Query("INSERT INTO gamejam.settings (guild_id, participant_role, manager_role, locale, team_size) " +
            "VALUES (:guildId, :participantRole, :managerRole, :locale, :teamSize) " +
            "ON CONFLICT (guild_id) DO UPDATE SET participant_role = :participantRole, manager_role = :managerRole, locale = :locale, team_size = :teamSize")
    void save(@NonNull Long guildId, @NonNull Long participantRole, @NonNull Long managerRole, @NonNull String locale, @NonNull Integer teamSize);

    @Query("SELECT guild_id, manager_role, participant_role, team_size, locale FROM gamejam.settings WHERE guild_id = :guildId")
    Optional<SettingsEntity> findById(long guildId);
}