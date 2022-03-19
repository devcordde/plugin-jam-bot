/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data;

import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.Team;
import de.chojo.gamejam.data.wrapper.team.TeamMember;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamData extends QueryFactoryHolder {

    private static final Logger log = getLogger(TeamData.class);

    public TeamData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err), err))
                .build());
    }

    public void createTeam(Jam jam, Team team) {
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
                .paramsBuilder(stmt -> stmt.setInt(jam.id()).setString(team.name()).setLong(team.leader()).setLong(team.roleId()).setLong(team.textChannelId()).setLong(team.voiceChannelId()))
                .insert();
    }

    public CompletableFuture<Optional<Team>> getTeamByMember(Jam jam, Member member) {
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
                .readRow(r -> r.getInt("team__id"))
                .first()
                .thenCompose(optId -> {
                    if (optId.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
                    return getTeamById(optId.get());
                });
    }

    public CompletableFuture<Optional<Team>> getTeamById(int id) {
        return builder(Team.class).query("SELECT id, jam_id, name, leader_id, role_id, text_channel_id, voice_channel_id FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .paramsBuilder(stmt -> stmt.setInt(id))
                .readRow(r -> new Team(r.getInt("id"),
                        r.getString("name"),
                        r.getLong("leader"),
                        r.getLong("role_id"),
                        r.getLong("text_channel_id"),
                        r.getLong("voice_channel_id")))
                .first();
    }

    public CompletableFuture<Boolean> joinTeam(Team team, Member member) {
        return builder()
                .query("INSERT INTO team_member(team_id, user_id) VALUES(?,?)")
                .paramsBuilder(p -> p.setInt(team.id()).setLong(member.getIdLong()))
                .insert()
                .execute()
                .thenApply(count -> count > 0);
    }

    public CompletableFuture<Boolean> leaveTeam(Team team, Member member) {
        return builder()
                .query("DELETE FROM team_member WHERE team_id = ? AND user_id = ?")
                .paramsBuilder(p -> p.setInt(team.id()).setLong(member.getIdLong()))
                .insert()
                .execute()
                .thenApply(count -> count > 0);
    }

    public CompletableFuture<Boolean> disbandTeam(Team team) {
        return builder()
                .query("DELETE FROM team WHERE id = ?")
                .paramsBuilder(p -> p.setInt(team.id()))
                .insert()
                .execute()
                .thenApply(count -> count > 0);
    }

    public CompletableFuture<List<TeamMember>> getMember(Team team) {
        return builder(TeamMember.class)
                .query("SELECT user_id FROM team_member WHERE team_id = ?")
                .paramsBuilder(p -> p.setInt(team.id()))
                .readRow(r -> new TeamMember(team, r.getLong("user_id")))
                .all();
    }
}
