/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.jam.JamMeta;
import de.chojo.gamejam.data.dao.guild.jams.jam.JamState;
import de.chojo.gamejam.data.dao.guild.jams.jam.JamTimes;
import de.chojo.gamejam.data.dao.guild.jams.jam.JamTeams;
import de.chojo.gamejam.data.dao.guild.jams.jam.user.JamUser;
import de.chojo.gamejam.data.wrapper.jam.TimeFrame;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.team.TeamVote;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Member;

import java.time.ZoneId;
import java.util.List;

public class Jam extends QueryFactory {
    private final JamGuild jamGuild;
    private final int id;
    private final JamTeams jamTeams;

    public Jam(JamGuild jamGuild, int id) {
        super(jamGuild);
        this.jamGuild = jamGuild;
        this.id = id;
        jamTeams = new JamTeams(this);
    }

    public void register(Member member) {
        builder()
                .query("INSERT INTO jam_registrations(jam_id, user_id) VALUES(?,?) ON CONFLICT DO NOTHING")
                .parameter(stmt -> stmt.setInt(id).setLong(member.getIdLong()))
                .insert()
                .sendSync();
    }

    public JamMeta meta() {
        return builder(JamMeta.class)
                .query("""
                       SELECT topic FROM jam_meta WHERE jam_id = ?
                       """)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(row -> new JamMeta(row.getString("topic")))
                .firstSync()
                .orElseThrow();
    }

    public List<Long> registrations() {
        return builder(Long.class)
                .query("SELECT user_id FROM jam_registrations WHERE jam_id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .map()
                .allSync();
    }

    public JamTimes times() {
        return builder(JamTimes.class)
                .query("""
                       SELECT registration_start,
                              registration_end,
                              zone_id,
                              jam_start,
                              jam_end
                       FROM jam_time
                       WHERE jam_id = ?
                       """)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(r -> {
                    var zone = ZoneId.of(r.getString("zone_id"));
                    return new JamTimes(zone,
                            TimeFrame.fromTimestamp(r.getTimestamp("registration_start"),
                                    r.getTimestamp("registration_end"), zone),
                            TimeFrame.fromTimestamp(r.getTimestamp("jam_start"),
                                    r.getTimestamp("jam_end"), zone)
                    );
                })
                .firstSync()
                .orElseThrow();
    }

    public JamState state() {
        return builder(JamState.class)
                .query("""
                       SELECT active,
                              voting,
                              ended
                       FROM jam_state
                       WHERE jam_id = ?
                       """)
                .parameter(stmt -> stmt.setInt(jamId()))
                .readRow(r -> new JamState(this, r.getBoolean("active"), r.getBoolean("voting"), r.getBoolean("ended")))
                .firstSync()
                .orElseThrow();
    }

    public List<TeamVote> votes() {
        return builder(TeamVote.class)
                .query("""
                       SELECT
                           rank, team_id, points, jam_id
                       FROM team_ranking r
                       WHERE r.jam_id = ?
                       """)
                .parameter(p -> p.setInt(id))
                .readRow(r -> new TeamVote(jamTeams.byId(r.getInt("team_id")).orElseThrow(), r.getInt("rank"), r.getInt("points")))
                .allSync();
    }

    public JamTeams teams() {
        return jamTeams;
    }

    public int jamId() {
        return id;
    }

    public JamGuild jamGuild() {
        return jamGuild;
    }

    public JamUser user(Member member) {
        return new JamUser(this, member);
    }
}
