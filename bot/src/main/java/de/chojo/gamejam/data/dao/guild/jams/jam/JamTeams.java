/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam;

import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.Optional;

public class JamTeams extends QueryFactory {
    private final Jam jam;

    public JamTeams(Jam jam) {
        super(jam);
        this.jam = jam;
    }

    public Team create(String name) {
        var teamId = builder(Integer.class)
                .query("""
                       INSERT INTO team(jam_id) VALUES(?) RETURNING id AS team_id;
                       """)
                .parameter(stmt -> stmt.setInt(jam.jamId()))
                .readRow(row -> row.getInt("team_id"))
                .firstSync()
                .orElseThrow();
        builder().query("""
                        INSERT INTO team_meta(team_id, team_name) VALUES (?,?);
                        """)
                 .parameter(stmt -> stmt.setInt(teamId).setString(name))
                 .insert()
                 .sendSync();
        return new Team(jam, teamId);
    }

    public List<Team> teams() {
        return builder(Team.class)
                .query("""
                       SELECT id,
                              team_name
                       FROM team t
                          LEFT JOIN team_meta m ON t.id = m.team_id
                       WHERE jam_id = ?
                       """)
                .parameter(stmt -> stmt.setInt(jam.jamId()))
                .readRow(r -> new Team(jam, r.getInt("id")))
                .allSync();
    }

    public Optional<Team> byMember(Member member) {
        return byMember(member.getUser());
    }

    public Optional<Team> byMember(User member) {
        return builder(Integer.class)
                .query("""
                       SELECT m.team_id
                       FROM team_member m
                           LEFT JOIN team t ON t.id = m.team_id
                           LEFT JOIN jam j ON j.id = t.jam_id
                       WHERE j.id = ?
                           AND user_id = ?
                       """)
                .parameter(p -> p.setInt(jam.jamId()).setLong(member.getIdLong()))
                .readRow(r -> r.getInt("team_id"))
                .firstSync()
                .flatMap(this::byId);
    }

    public Optional<Team> byName(String name) {
        return builder(Integer.class)
                .query("""
                       SELECT id FROM team t LEFT JOIN team_meta m ON t.id = m.team_id
                       WHERE jam_id = ?
                           AND LOWER(m.team_name) = LOWER(?)
                       """)
                .parameter(p -> p.setInt(jam.jamId()).setString(name))
                .readRow(r -> r.getInt("id"))
                .firstSync()
                .flatMap(this::byId);
    }

    public Optional<Team> byId(int id) {
        return builder(Team.class)
                .query("SELECT id, team_name FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .readRow(r -> new Team(jam, r.getInt("id")))
                .firstSync();
    }

    public List<Command.Choice> completeTeam(String name) {
        if (name.isBlank()) {
            return teams().stream()
                          .map(team -> team.meta().name())
                          .map(team -> new Command.Choice(team, team))
                          .toList();
        }

        return teams().stream()
                      .filter(team -> team.matchName(name))
                      .map(team -> team.meta().name())
                      .map(team -> new Command.Choice(team, team))
                      .toList();
    }
}
