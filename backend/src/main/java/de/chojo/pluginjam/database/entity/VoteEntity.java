package de.chojo.pluginjam.database.entity;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "vote", schema = "gamejam")
public record VoteEntity(
        @MappedProperty("team_id")
        Long teamId,
        @MappedProperty("voter_id")
        Long voterId,
        Integer points
) {
}
