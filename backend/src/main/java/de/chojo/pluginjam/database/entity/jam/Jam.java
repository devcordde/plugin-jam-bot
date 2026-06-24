package de.chojo.pluginjam.database.entity.jam;

import io.micronaut.data.annotation.*;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@MappedEntity(value = "jam", schema = "gamejam")
public record Jam(
        @Id
        @GeneratedValue
        Integer id,
        @MappedProperty("guild_id")
        Long guildId,
        @Relation(value = Relation.Kind.ONE_TO_ONE, mappedBy = "jamId")
        JamMeta meta,
        @Relation(value = Relation.Kind.ONE_TO_ONE, mappedBy = "jamId")
        JamTime time,
        @Relation(value = Relation.Kind.ONE_TO_ONE, mappedBy = "jamId")
        JamState state,
        @Relation(value = Relation.Kind.ONE_TO_MANY, mappedBy = "jamId")
        List<JamRegistration> registrations
) {
}
