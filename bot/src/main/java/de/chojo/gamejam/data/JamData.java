/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data;

import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.jam.JamBuilder;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.gamejam.data.wrapper.jam.JamState;
import de.chojo.gamejam.data.wrapper.jam.JamTimes;
import de.chojo.gamejam.data.wrapper.jam.TimeFrame;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class JamData extends QueryFactoryHolder {
    private static final Logger log = getLogger(JamData.class);

    public JamData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err), err))
                .build());
    }

    public CompletableFuture<JamSettings> getSettings(Guild guild) {
        return builder(JamSettings.class)
                .query("SELECT jam_role, team_size FROM jam_settings WHERE guild_id = ?")
                .paramsBuilder(p -> p.setLong(guild.getIdLong()))
                .readRow(r -> new JamSettings(r.getInt("team_size"), r.getLong("jam_role")))
                .first()
                .thenApply(r -> r.orElse(new JamSettings()));
    }

    public CompletableFuture<Boolean> updateSettings(Guild guild, JamSettings settings) {
        return builder(JamSettings.class)
                .query("""
                        INSERT INTO jam_settings(guild_id, jam_role, team_size, manager_role) VALUES (?,?,?,?)
                        ON CONFLICT(guild_id)
                            DO UPDATE
                                SET jam_role = excluded.jam_role, team_size = excluded.team_size, manager_role = excluded.manager_role;
                        """)
                .paramsBuilder(p -> p.setLong(guild.getIdLong()).setLong(settings.jamRole()).setInt(settings.teamSize()).setLong(settings.orgaRole()))
                .update()
                .execute()
                .thenApply(r -> r > 0);
    }

    public void createJam(Jam jam, Guild guild) {
        builder(Integer.class).query("INSERT INTO jam(guild_id) VALUES(?) RETURNING id")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(r -> r.getInt("id"))
                .first()
                .thenApply(id -> {
                    var times = jam.times();
                    builder().query("""
                                    INSERT INTO jam_time(
                                    jam_id,
                                    registration_start, registration_end,
                                    jam_start, jam_end,
                                    zone_id)
                                     VALUES (?,?,?,?,?,?)
                                    """)
                            .paramsBuilder(stmt -> stmt.setInt(id.get())
                                    .setTimestamp(times.registration().startTimestamp())
                                    .setTimestamp(times.registration().endTimestamp())
                                    .setTimestamp(times.jam().startTimestamp())
                                    .setTimestamp(times.jam().endTimestamp())
                                    .setString(times.zone().getId())
                            ).append()
                            .query("INSERT INTO jam_meta(jam_id, topic) VALUES(?,?)")
                            .paramsBuilder(stmt -> stmt.setInt(id.get()).setString(jam.topic()))
                            .append()
                            .query("INSERT INTO jam_state(jam_id) VALUES(?)")
                            .paramsBuilder(stmt -> stmt.setInt(id.get()))
                            .insert()
                            .execute();
                    return id;
                });
    }

    public void register(Jam jam, Member member) {
        builder()
                .query("INSERT INTO jam_registrations(jam_id, user_id) VALUES(?,?) ON CONFLICT DO NOTHING")
                .paramsBuilder(stmt -> stmt.setInt(jam.id()).setLong(member.getIdLong()))
                .insert()
                .execute();
    }

    public CompletableFuture<Optional<JamBuilder>> getCurrentJam(Guild guild) {
        return builder(JamBuilder.class)
                .query("""
                        SELECT
                            id
                        FROM jam_time t
                        LEFT JOIN jam j ON j.id = t.jam_id
                        WHERE registration_start < NOW() AT TIME ZONE 'utc'
                            AND t.jam_end > NOW() AT TIME ZONE 'utc'
                            AND guild_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(r -> new JamBuilder(r.getInt("id")))
                .first();
    }

    public CompletableFuture<Optional<Jam>> getNextOrCurrentJam(Guild guild) {
        return builder(Integer.class)
                .query("""
                        SELECT
                            id
                        FROM jam_time t
                        LEFT JOIN jam j ON j.id = t.jam_id
                        WHERE t.jam_end > NOW() AT TIME ZONE 'utc'
                            AND guild_id = ?
                        ORDER BY t.jam_end ASC
                        LIMIT 1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(r -> r.getInt("id"))
                .first()
                .thenCompose(id -> {
                    if (id.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
                    return getJamById(id.get());
                });
    }

    public CompletableFuture<Optional<Jam>> getActiveJam(Guild guild) {
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
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(r -> r.getInt("id"))
                .first()
                .thenCompose(id -> {
                    if (id.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
                    return getJamById(id.get());
                });
    }

    public void updateJamState(Jam jam) {
        var state = jam.state();
        builder().query("""
                        UPDATE jam_state
                        SET active = ?, voting = ?, ended = ?
                        WHERE jam_id = ?
                        """).paramsBuilder(p -> p.setBoolean(state.isActive()).setBoolean(state.isVoting()).setBoolean(state.hasEnded()).setInt(jam.id()))
                .update()
                .execute();
    }

    public CompletableFuture<Optional<Jam>> getJamById(int id) {
        return builder(JamBuilder.class)
                .query("""      
                        SELECT
                            j.id,
                            guild_id,
                            registration_start,
                            registration_end,
                            jam_start,
                            jam_end,
                            zone_id,
                            topic,
                            active,
                            voting,
                            ended
                        FROM jam j
                        LEFT JOIN jam_time t ON j.id = t.jam_id
                        LEFT JOIN jam_meta m ON j.id = m.jam_id
                        LEFT JOIN jam_state s ON j.id = s.jam_id
                        WHERE m.jam_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setInt(id))
                .readRow(r -> {
                    var zone = ZoneId.of(r.getString("zone_id"));
                    return new JamBuilder(r.getInt("id"))
                            .setTopic(r.getString("topic"))
                            .setTimes(new JamTimes(zone,
                                    TimeFrame.fromTimestamp(r.getTimestamp("registration_start"),
                                            r.getTimestamp("registration_end"), zone),
                                    TimeFrame.fromTimestamp(r.getTimestamp("jam_start"),
                                            r.getTimestamp("jam_end"), zone)
                            )).setState(new JamState(r.getBoolean("active"), r.getBoolean("voting"), r.getBoolean("ended")));
                })
                .first()
                .thenApply(optJam -> {
                    if (optJam.isEmpty()) return Optional.empty();
                    var jam = optJam.get();
                    var registered = builder(Long.class).query("SELECT user_id FROM jam_registrations WHERE jam_id = ?")
                            .paramsBuilder(stmt -> stmt.setInt(jam.id()))
                            .readRow(r -> r.getLong("user_id"))
                            .allSync();
                    var teams = builder(JamTeam.class).query("SELECT id, jam_id, name, leader_id, role_id, text_channel_id, voice_channel_id FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE jam_id = ?")
                            .paramsBuilder(stmt -> stmt.setInt(jam.id()))
                            .readRow(r -> new JamTeam(r.getInt("id"),
                                    r.getString("name"),
                                    r.getLong("leader_id"),
                                    r.getLong("role_id"),
                                    r.getLong("text_channel_id"),
                                    r.getLong("voice_channel_id")))
                            .allSync();
                    return Optional.of(jam.setRegistrations(registered).setTeams(teams).build());
                });
    }
}
