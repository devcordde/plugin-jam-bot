/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.database.entity.jam;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity(value = "jam_meta", schema = "gamejam")
public record JamMeta(
        @Id
        @MappedProperty("jam_id")
        Integer jamId,
        String topic,
        String tagline
) {
}
