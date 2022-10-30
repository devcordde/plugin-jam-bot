/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.wrapper.jam.JamCreator;
import de.chojo.sadu.base.QueryFactory;

import java.util.Optional;

public class Jams extends QueryFactory {

    private final JamGuild jamGuild;

    public Jams(JamGuild jamGuild) {
        super(jamGuild);
        this.jamGuild = jamGuild;
    }

    public void create(JamCreator jamCreator) {
        builder(Integer.class)
                .query("INSERT INTO jam(guild_id) VALUES(?) RETURNING id")
                .parameter(stmt -> stmt.setLong(jamGuild.guildId()))
                .readRow(r -> r.getInt("id"))
                .firstSync()
                .map(id -> {
                    var times = jamCreator.times();
                    builder().query("""
                                    INSERT INTO jam_time(
                                    jam_id,
                                    registration_start, registration_end,
                                    jam_start, jam_end,
                                    zone_id)
                                     VALUES (?,?,?,?,?,?)
                                    """)
                             .parameter(stmt -> stmt.setInt(id)
                                                    .setTimestamp(times.registration().startTimestamp())
                                                    .setTimestamp(times.registration().endTimestamp())
                                                    .setTimestamp(times.jam().startTimestamp())
                                                    .setTimestamp(times.jam().endTimestamp())
                                                    .setString(times.zone().getId())
                             ).append()
                             .query("INSERT INTO jam_meta(jam_id, topic) VALUES(?,?)")
                             .parameter(stmt -> stmt.setInt(id).setString(jamCreator.topic()))
                             .append()
                             .query("INSERT INTO jam_state(jam_id) VALUES(?)")
                             .parameter(stmt -> stmt.setInt(id))
                             .insert()
                             .sendSync();
                    return id;
                });
    }

    public Optional<Jam> getCurrentJam() {
        return builder(Jam.class)
                .query("""
                       SELECT
                           id
                       FROM jam_time t
                       LEFT JOIN jam j ON j.id = t.jam_id
                       WHERE registration_start < NOW() AT TIME ZONE 'utc'
                           AND t.jam_end > NOW() AT TIME ZONE 'utc'
                           AND guild_id = ?;
                       """)
                .parameter(stmt -> stmt.setLong(jamGuild.guildId()))
                .readRow(r -> new Jam(jamGuild, r.getInt("id")))
                .firstSync();
    }

    public Optional<Jam> nextOrCurrent() {
        return builder(Integer.class)
                .query("""
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
                .parameter(stmt -> stmt.setLong(jamGuild.guildId()))
                .readRow(r -> r.getInt("id"))
                .firstSync()
                .flatMap(this::byId);
    }

    public Optional<Jam> activeJam() {
        return builder(Integer.class)
                .query("""
                       SELECT
                           id
                       FROM jam_state s
                       LEFT JOIN jam j ON j.id = s.jam_id
                       WHERE s.active
                           AND guild_id = ?
                       LIMIT 1;
                       """)
                .parameter(stmt -> stmt.setLong(jamGuild.guildId()))
                .readRow(r -> r.getInt("id"))
                .firstSync()
                .flatMap(this::byId);
    }

    public Optional<Jam> byId(int id) {
        return builder(Jam.class)
                .query("""
                       SELECT id FROM jam WHERE guild_id = ? AND id = ?
                       """)
                .parameter(stmt -> stmt.setLong(jamGuild.guildId()).setInt(id))
                .readRow(row -> new Jam(jamGuild, id))
                .firstSync();
    }
}
