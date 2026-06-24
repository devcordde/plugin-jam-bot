package de.chojo.pluginjam.service;

import de.chojo.pluginjam.database.entity.jam.Jam;
import de.chojo.pluginjam.database.repository.jam.JamRegistrationRepository;
import de.chojo.pluginjam.database.repository.jam.JamRepository;
import de.chojo.pluginjam.database.repository.SettingsRepository;
import jakarta.inject.Singleton;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class JamService {
    private final JamRepository jamRepository;
    private final JamRegistrationRepository jamRegistrationRepository;
    private final SettingsRepository settingsRepository;

    public JamService(JamRepository jamRepository, JamRegistrationRepository jamRegistrationRepository, SettingsRepository settingsRepository) {
        this.jamRepository = jamRepository;
        this.jamRegistrationRepository = jamRegistrationRepository;
        this.settingsRepository = settingsRepository;
    }

    public void createJam(
            long guildId,
            String topic,
            String tagline,
            String zoneId,
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            ZonedDateTime registrationStartDate,
            ZonedDateTime registrationEndDate
    ) {
        jamRepository.createJam(
                guildId,
                topic,
                tagline,
                zoneId,
                registrationStartDate.toLocalDateTime(),
                registrationEndDate.toLocalDateTime(),
                startDate.toLocalDateTime(),
                endDate.toLocalDateTime()
        );
    }

    public boolean isJamActive(long guildId) {
        return jamRepository.findByGuildIdAndStateActive(guildId, true).isPresent();
    }

    public Optional<Jam> getJamById(int jamId) {
        return jamRepository.findById(jamId);
    }

    public Optional<Jam> getActiveJam(long guildId) {
        return jamRepository.findByGuildIdAndStateActive(guildId, true);
    }

    public Optional<Jam> getUpComingJam(long guildId) {
        return jamRepository.findByGuildIdAndStateActiveAndStateEnded(guildId, false, false)
                .stream()
                .findFirst();
    }

    public void endActiveJams(long guildId) {
        jamRepository.endActiveJams(guildId);
    }

    public Optional<Jam> getCurrentOrUpcoming(long guildId) {
        return getUpComingJam(guildId)
                .or(() -> getActiveJam(guildId));
    }

    public void setVoting(int jamId, boolean voting) {
        jamRepository.updateVoting(jamId, voting);
    }

    public void startJam(int id) {
        jamRepository.updateState(id, true, false, false);
    }

    public List<Jam> getJams(long guildId) {
        return jamRepository.findByGuildId(guildId);
    }

    public void registerUser(int jamId, long userId) {
        jamRegistrationRepository.save(jamId, userId);
    }

    public void unregisterUser(int jamId, long userId) {
        jamRegistrationRepository.delete(jamId, userId);
    }
}
