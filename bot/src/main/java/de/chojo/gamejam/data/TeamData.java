/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data;

import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.gamejam.data.wrapper.team.TeamMember;
import de.chojo.gamejam.data.wrapper.votes.TeamVote;
import de.chojo.gamejam.data.wrapper.votes.VoteEntry;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamData extends QueryFactoryHolder {

    private static final Logger log = getLogger(TeamData.class);

    public TeamData(DataSource dataSource, QueryBuilderConfig config) {
        super(dataSource, config);
    }

    public void createTeam(Jam jam, JamTeam jamTeam) {
        builder()
                .query("""
                        WITH id AS (
                            INSERT INTO team(jam_id) VALUES(?) RETURNING id AS team_id
                        ),
                        leader AS(
                            INSERT INTO team_meta(team_id, name, leader_id, role_id, text_channel_id, voice_channel_id)
                            VALUES ((SELECT team_id FROM id),?,?,?,?,?) RETURNING leader_id
                        )
                        INSERT INTO team_member(team_id, user_id) VALUES((SELECT team_id FROM id), (SELECT leader_id FROM leader))
                        """)
                .paramsBuilder(stmt -> stmt.setInt(jam.id())
                        .setString(jamTeam.name()).setLong(jamTeam.leader()).setLong(jamTeam.roleId()).setLong(jamTeam.textChannelId()).setLong(jamTeam.voiceChannelId()))
                .insert()
                .execute();
    }

    public CompletableFuture<Optional<JamTeam>> getTeamByMember(Jam jam, Member member) {
        return getTeamByMember(jam, member.getUser());
    }

    public CompletableFuture<Optional<JamTeam>> getTeamByMember(Jam jam, User member) {
        return builder(Integer.class).query("""
                        SELECT
                            m.team_id
                        FROM team_member m
                        LEFT JOIN team t ON t.id = m.team_id
                        LEFT JOIN jam j ON j.id = t.jam_id
                        WHERE j.id = ?
                            AND user_id = ?
                        """)
                .paramsBuilder(p -> p.setInt(jam.id()).setLong(member.getIdLong()))
                .readRow(r -> r.getInt("team_id"))
                .first()
                .thenCompose(optId -> {
                    if (optId.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
                    return getTeamById(optId.get());
                });
    }

    public CompletableFuture<Optional<JamTeam>> getTeamById(int id) {
        return builder(JamTeam.class).query("SELECT id, jam_id, name, leader_id, role_id, text_channel_id, voice_channel_id FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .paramsBuilder(stmt -> stmt.setInt(id))
                .readRow(r -> new JamTeam(r.getInt("id"),
                        r.getString("name"),
                        r.getLong("leader_id"),
                        r.getLong("role_id"),
                        r.getLong("text_channel_id"),
                        r.getLong("voice_channel_id")))
                .first();
    }

    public CompletableFuture<Boolean> joinTeam(JamTeam jamTeam, Member member) {
        return builder()
                .query("INSERT INTO team_member(team_id, user_id) VALUES(?,?)")
                .paramsBuilder(p -> p.setInt(jamTeam.id()).setLong(member.getIdLong()))
                .insert()
                .execute()
                .thenApply(count -> count > 0);
    }

    public CompletableFuture<Boolean> leaveTeam(JamTeam jamTeam, Member member) {
        return builder()
                .query("DELETE FROM team_member WHERE team_id = ? AND user_id = ?")
                .paramsBuilder(p -> p.setInt(jamTeam.id()).setLong(member.getIdLong()))
                .insert()
                .execute()
                .thenApply(count -> count > 0);
    }

    public CompletableFuture<Boolean> disbandTeam(JamTeam jamTeam) {
        return builder()
                .query("DELETE FROM team WHERE id = ?")
                .paramsBuilder(p -> p.setInt(jamTeam.id()))
                .insert()
                .execute()
                .thenApply(count -> count > 0);
    }

    public CompletableFuture<List<TeamMember>> getMember(JamTeam jamTeam) {
        return builder(TeamMember.class)
                .query("SELECT user_id FROM team_member WHERE team_id = ?")
                .paramsBuilder(p -> p.setInt(jamTeam.id()))
                .readRow(r -> new TeamMember(jamTeam, r.getLong("user_id")))
                .all();
    }

    public CompletableFuture<Optional<JamTeam>> getTeamByName(Jam jam, String name) {
        return builder(Integer.class)
                .query("""
                        SELECT id FROM team t LEFT JOIN team_meta m ON t.id = m.team_id
                        WHERE jam_id = ?
                            AND LOWER(m.name) = LOWER(?)
                        """)
                .paramsBuilder(p -> p.setInt(jam.id()).setString(name))
                .readRow(r -> r.getInt("id"))
                .first()
                .thenCompose(id -> {
                    if (id.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
                    return getTeamById(id.get());
                });
    }

    public CompletableFuture<Boolean> updateTeam(JamTeam team) {
        return builder().query("UPDATE team_meta SET name = ?, leader_id = ? WHERE team_id = ?")
                .paramsBuilder(p -> p.setString(team.name()).setLong(team.leader()).setInt(team.id()))
                .update()
                .execute()
                .thenApply(row -> row > 0);
    }

    public CompletableFuture<List<VoteEntry>> votesByUser(Member member, Jam jam) {
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
                .paramsBuilder(p -> p.setInt(jam.id()).setLong(member.getIdLong()))
                .readRow(r -> new VoteEntry(jam.team(r.getInt("id")), r.getLong("voter_id"), r.getInt("points")))
                .all();
    }

    public CompletableFuture<List<TeamVote>> votesByJam(Jam jam) {
        return builder(TeamVote.class)
                .query("""
                        SELECT
                            v.team_id,
                            SUM(v.points) AS points
                        FROM vote v LEFT JOIN team t ON t.id = v.team_id
                        WHERE t.jam_id = ?
                        GROUP BY team_id
                        ORDER BY points DESC
                        """)
                .paramsBuilder(p -> p.setInt(jam.id()))
                .readRow(r -> new TeamVote(jam.team(r.getInt("id")), r.getInt("points")))
                .all();
    }

    public CompletableFuture<List<TeamVote>> votesByTeam(JamTeam team) {
        return builder(TeamVote.class)
                .query("""
                        SELECT
                            SUM(v.points) AS points
                        FROM vote v LEFT JOIN team t ON t.id = v.team_id
                        WHERE v.team_id = ?
                        """)
                .paramsBuilder(p -> p.setInt(team.id()))
                .readRow(r -> new TeamVote(team, r.getInt("points")))
                .all();
    }

    public CompletableFuture<Boolean> vote(Member member, JamTeam team, int points) {
        return builder()
                .query("""
                        INSERT INTO vote(team_id, voter_id, points) VALUES (?,?,?)
                        ON CONFLICT (team_id, voter_id)
                            DO UPDATE SET points = excluded.points;
                        """)
                .paramsBuilder(p -> p.setInt(team.id()).setLong(member.getIdLong()).setInt(points))
                .insert()
                .execute()
                .thenApply(r -> r > 0);
    }
}
