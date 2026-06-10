package de.chojo.pluginjam.database.entity.team;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "team_meta", schema = "gamejam")
public record TeamMeta(
        @Id
        @MappedProperty("team_id")
        Integer teamId,
        @MappedProperty("team_name")
        String teamName,
        @MappedProperty("leader_id")
        Long leaderId,
        @MappedProperty("role_id")
        Long roleId,
        @MappedProperty("text_channel_id")
        Long textChannelId,
        @MappedProperty("voice_channel_id")
        Long voiceChannelId,
        @MappedProperty("project_description")
        String projectDescription,
        @MappedProperty("project_url")
        String projectUrl,
        String token
) {
}
