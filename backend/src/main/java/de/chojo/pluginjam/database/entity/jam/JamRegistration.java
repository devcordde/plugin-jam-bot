package de.chojo.pluginjam.database.entity.jam;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "jam_registrations", schema = "gamejam")
public record JamRegistration(
        @MappedProperty("jam_id")
        Integer jamId,
        @Id
        @MappedProperty("user_id")
        Long userId
) {
}
