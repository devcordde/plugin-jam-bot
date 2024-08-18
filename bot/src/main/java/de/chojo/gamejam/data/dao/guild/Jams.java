/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.wrapper.jam.JamCreator;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;

import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Jams {

    private final JamGuild jamGuild;

    public Jams(JamGuild jamGuild) {
        this.jamGuild = jamGuild;
    }

    public void create(JamCreator jamCreator) {
        try (var conf = QueryConfiguration.getDefault().withSingleTransaction()) {
            conf.query("INSERT INTO jam(guild_id) VALUES(?) RETURNING id")
                    .single(call().bind(jamGuild.guildId()))
                    .map(r -> r.getInt("id"))
                    .first()
                    .map(id -> {
                        var times = jamCreator.times();
                        conf.query("""
                                        INSERT INTO jam_time(
                                        jam_id,
                                        registration_start, registration_end,
                                        jam_start, jam_end,
                                        zone_id)
                                         VALUES (?,?,?,?,?,?)
                                        """)
                                .single(call().bind(id)
                                        .bind(times.registration().startTimestamp())
                                        .bind(times.registration().endTimestamp())
                                        .bind(times.jam().startTimestamp())
                                        .bind(times.jam().endTimestamp())
                                        .bind(times.zone().getId())
                                )
                                .insert();
                        conf.query("INSERT INTO jam_meta(jam_id, topic) VALUES(?,?)")
                                .single(call().bind(id).bind(jamCreator.topic()))
                                .insert();
                        conf.query("INSERT INTO jam_state(jam_id) VALUES(?)")
                                .single(call().bind(id))
                                .insert();
                        return id;
                    });
        }
    }

    public Optional<Jam> getCurrentJam() {
        return query("""
                SELECT
                    id
                FROM jam_time t
                LEFT JOIN jam j ON j.id = t.jam_id
                WHERE registration_start < NOW() AT TIME ZONE 'utc'
                    AND t.jam_end > NOW() AT TIME ZONE 'utc'
                    AND guild_id = ?;
                """)
                .single(call().bind(jamGuild.guildId()))
                .map(r -> new Jam(jamGuild, r.getInt("id")))
                .first();
    }

    public Optional<Jam> nextOrCurrent() {
        return query("""
                SELECT
                    id
                FROM jam_time t
                LEFT JOIN jam j ON j.id = t.jam_id
                LEFT JOIN jam_state js ON j.id = js.jam_id
                WHERE js.active OR t.jam_end > NOW() AT TIME ZONE 'utc'
                    AND guild_id = ?
                ORDER BY t.jam_end ASC
                LIMIT 1;
                """)
                .single(call().bind(jamGuild.guildId()))
                .map(r -> r.getInt("id"))
                .first()
                .flatMap(this::byId);
    }

    public Optional<Jam> activeJam() {
        return query("""
                SELECT
                    id
                FROM jam_state s
                LEFT JOIN jam j ON j.id = s.jam_id
                WHERE s.active
                    AND guild_id = ?
                LIMIT 1;
                """)
                .single(call().bind(jamGuild.guildId()))
                .map(r -> r.getInt("id"))
                .first()
                .flatMap(this::byId);
    }

    public Optional<Jam> byId(int id) {
        return query("""
                SELECT id FROM jam WHERE guild_id = ? AND id = ?
                """)
                .single(call().bind(jamGuild.guildId()).bind(id))
                .map(row -> new Jam(jamGuild, id))
                .first();
    }
}
