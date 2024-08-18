/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.user;

import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.data.wrapper.votes.VoteEntry;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class JamUser {
    private final Jam jam;
    private final Member member;

    public JamUser(Jam jam, Member member) {
        this.jam = jam;
        this.member = member;
    }

    public List<VoteEntry> votes() {
        return query("""
                SELECT
                    v.team_id,
                    v.voter_id,
                    v.points
                FROM vote v
                LEFT JOIN team t ON t.id = v.team_id
                WHERE t.jam_id = ?
                    AND voter_id = ?
                """)
                .single(call().bind(jam.jamId()).bind(member.getIdLong()))
                .map(r -> new VoteEntry(jam.teams().byId(r.getInt("team_id")).orElseThrow(),
                        r.getLong("voter_id"), r.getInt("points")))
                .all();
    }

    public int votesGiven() {
        return query("""
                SELECT
                    sum(points) as points
                FROM vote v
                LEFT JOIN team t ON t.id = v.team_id
                WHERE t.jam_id = ?
                    AND voter_id = ?
                """)
                .single(call().bind(jam.jamId()).bind(member.getIdLong()))
                .map(r -> r.getInt("points"))
                .first()
                .orElse(0);
    }

    public Optional<Team> team() {
        return jam.teams().byMember(member);
    }

    public boolean join(Team team) {
        var guild = team.jam().jamGuild().guild();
        var roleById = team.meta().role();

        roleById.ifPresent(role -> guild.addRoleToMember(member, role).queue());

        return query("INSERT INTO team_member(team_id, user_id) VALUES(?,?)")
                .single(call().bind(team.id()).bind(member.getIdLong()))
                .insert()
                .changed();
    }
}
