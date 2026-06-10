package de.chojo.pluginjam.database.entity;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "settings", schema = "gamejam")
public final class SettingsEntity {
    @Id
    @MappedProperty("guild_id")
    private Long guildId;
    @MappedProperty("manager_role")
    private Long orgaRole;
    @MappedProperty("participant_role")
    private Long participantRole;
    @MappedProperty("locale")
    private String locale;
    @MappedProperty("team_size")
    private Integer teamSize;

    public SettingsEntity(
            @Id
            Long guildId,
            Long orgaRole,
            Long participantRole,
            String locale,
            Integer teamSize
    ) {
        this.guildId = guildId;
        this.orgaRole = orgaRole;
        this.participantRole = participantRole;
        this.locale = locale;
        this.teamSize = teamSize;
    }

    public SettingsEntity(long guildId) {
        this.guildId = guildId;
        this.orgaRole = 0L;
        this.participantRole = 0L;
        this.locale = "en";
        this.teamSize = 3;
    }

    public Long getGuildId() {
        return guildId;
    }

    public Long getOrgaRole() {
        return orgaRole;
    }

    public Long getParticipantRole() {
        return participantRole;
    }

    public String getLocale() {
        return locale;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public void setOrgaRole(Long orgaRole) {
        this.orgaRole = orgaRole;
    }

    public void setParticipantRole(Long participantRole) {
        this.participantRole = participantRole;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }
}
