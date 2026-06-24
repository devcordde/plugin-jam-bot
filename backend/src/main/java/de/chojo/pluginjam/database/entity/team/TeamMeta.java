package de.chojo.pluginjam.database.entity.team;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "team_meta", schema = "gamejam")
public final class TeamMeta {
    @Id
    @MappedProperty("team_id")
    private Integer teamId;
    @MappedProperty("team_name")
    private String teamName;
    @MappedProperty("leader_id")
    private Long leaderId;
    @MappedProperty("role_id")
    private Long roleId;
    @MappedProperty("text_channel_id")
    private Long textChannelId;
    @MappedProperty("voice_channel_id")
    private Long voiceChannelId;
    @MappedProperty("project_description")
    private String projectDescription;
    @MappedProperty("project_url")
    private String projectUrl;
    private String token;

    public TeamMeta(
            @Id
            Integer teamId,
            String teamName,
            Long leaderId,
            Long roleId,
            Long textChannelId,
            Long voiceChannelId,
            String projectDescription,
            String projectUrl,
            String token
    ) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.leaderId = leaderId;
        this.roleId = roleId;
        this.textChannelId = textChannelId;
        this.voiceChannelId = voiceChannelId;
        this.projectDescription = projectDescription;
        this.projectUrl = projectUrl;
        this.token = token;
    }

    @Id
    @MappedProperty("team_id")
    public Integer getTeamId() {
        return teamId;
    }

    @MappedProperty("team_name")
    public String getTeamName() {
        return teamName;
    }

    @MappedProperty("leader_id")
    public Long getLeaderId() {
        return leaderId;
    }

    @MappedProperty("role_id")
    public Long getRoleId() {
        return roleId;
    }

    @MappedProperty("text_channel_id")
    public Long getTextChannelId() {
        return textChannelId;
    }

    @MappedProperty("voice_channel_id")
    public Long getVoiceChannelId() {
        return voiceChannelId;
    }

    @MappedProperty("project_description")
    public String getProjectDescription() {
        return projectDescription;
    }

    @MappedProperty("project_url")
    public String getProjectUrl() {
        return projectUrl;
    }

    public String getToken() {
        return token;
    }

    public void getTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void getLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public void getRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void getTextChannelId(Long textChannelId) {
        this.textChannelId = textChannelId;
    }

    public void getVoiceChannelId(Long voiceChannelId) {
        this.voiceChannelId = voiceChannelId;
    }

    public void getProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public void getProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public void token(String token) {
        this.token = token;
    }
}
