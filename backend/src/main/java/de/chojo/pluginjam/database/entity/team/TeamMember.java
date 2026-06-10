package de.chojo.pluginjam.database.entity.team;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "team_member", schema = "gamejam")
public record TeamMember(
        @MappedProperty("team_id")
        Integer teamId,
        @Id
        @MappedProperty("user_id")
        Long userId
) {
}
