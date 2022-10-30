/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.user;

import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.data.wrapper.votes.VoteEntry;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Optional;

public class JamUser extends QueryFactory {
    private final Jam jam;
    private final Member member;

    public JamUser(Jam jam, Member member) {
        super(jam);
        this.jam = jam;
        this.member = member;
    }

    public List<VoteEntry> votes() {
        return builder(VoteEntry.class)
                .query("""
                       SELECT
                           v.team_id,
                           v.voter_id,
                           v.points
                       FROM vote v
                       LEFT JOIN team t ON t.id = v.team_id
                       WHERE t.jam_id = ?
                           AND voter_id = ?
                       """)
                .parameter(p -> p.setInt(jam.jamId()).setLong(member.getIdLong()))
                .readRow(r -> new VoteEntry(jam.teams().byId(r.getInt("team_id")).orElseThrow(),
                        r.getLong("voter_id"), r.getInt("points")))
                .allSync();
    }

    public int votesGiven() {
        return builder(Integer.class)
                .query("""
                       SELECT
                           sum(points)
                       FROM vote v
                       LEFT JOIN team t ON t.id = v.team_id
                       WHERE t.jam_id = ?
                           AND voter_id = ?
                       """)
                .parameter(p -> p.setInt(jam.jamId()).setLong(member.getIdLong()))
                .readRow(r -> r.getInt("points"))
                .firstSync()
                .orElse(0);
    }

    public Optional<Team> team() {
        return jam.teams().byMember(member);
    }

    public boolean join(Team team) {
        var guild = team.jam().jamGuild().guild();
        var roleById = team.meta().role();

        roleById.ifPresent(role -> guild.addRoleToMember(member, role).queue());

        return builder()
                .query("INSERT INTO team_member(team_id, user_id) VALUES(?,?)")
                .parameter(p -> p.setInt(team.id()).setLong(member.getIdLong()))
                .insert()
                .sendSync()
                .changed();
    }
}
