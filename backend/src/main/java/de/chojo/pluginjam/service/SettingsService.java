package de.chojo.pluginjam.service;

import de.chojo.pluginjam.database.entity.SettingsEntity;
import de.chojo.pluginjam.database.repository.SettingsRepository;
import jakarta.inject.Singleton;

@Singleton
public class SettingsService {
    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public SettingsEntity getSettings(long guildId) {
        return settingsRepository.findById(guildId).orElseGet(() -> new SettingsEntity(guildId));
    }

    public void saveSettings(SettingsEntity settings) {
        settingsRepository.save(settings.getGuildId(), settings.getParticipantRole(), settings.getOrgaRole(), settings.getLocale(), settings.getTeamSize());
    }

    public void setJamRole(long guildId, long roleId) {
        var settings = settingsRepository.findById(guildId).orElseGet(() -> new SettingsEntity(guildId));
        settings.setParticipantRole(roleId);
        settingsRepository.save(settings.getGuildId(), settings.getParticipantRole(), settings.getOrgaRole(), settings.getLocale(), settings.getTeamSize());
    }

    public void setOrgaRole(long guildId, long roleId) {
        var settings = settingsRepository.findById(guildId).orElseGet(() -> new SettingsEntity(guildId));
        settings.setOrgaRole(roleId);
        settingsRepository.save(settings.getGuildId(), settings.getParticipantRole(), settings.getOrgaRole(), settings.getLocale(), settings.getTeamSize());
    }

    public void setTeamSize(long guildId, int teamSize) {
        var settings = settingsRepository.findById(guildId).orElseGet(() -> new SettingsEntity(guildId));
        settings.setTeamSize(teamSize);
        settingsRepository.save(settings.getGuildId(), settings.getParticipantRole(), settings.getOrgaRole(), settings.getLocale(), settings.getTeamSize());
    }
}
