package de.chojo.pluginjam.database.entity.team;

import io.micronaut.data.annotation.*;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@MappedEntity(value = "team", schema = "gamejam")
public record Team(
        @Id
        @GeneratedValue(GeneratedValue.Type.AUTO)
        Integer id,
        @MappedProperty("jam_id")
        Integer jamId,
        @Relation(value = Relation.Kind.ONE_TO_ONE, mappedBy = "teamId")
        TeamMeta meta,
        @Relation(value = Relation.Kind.ONE_TO_MANY, mappedBy = "teamId")
        List<TeamMember> members
) {
}
