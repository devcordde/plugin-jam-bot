package de.chojo.pluginjam.database.entity.jam;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
@MappedEntity(value = "jam_time", schema = "gamejam")
public record JamTime(
        @Id
        @MappedProperty("jam_id")
        Integer jamId,
        @MappedProperty("zone_id")
        String zoneId,
        @MappedProperty("registration_start")
        LocalDateTime registrationStart,
        @MappedProperty("registration_end")
        LocalDateTime registrationEnd,
        @MappedProperty("jam_start")
        LocalDateTime jamStart,
        @MappedProperty("jam_end")
        LocalDateTime jamEnd
) {
}
