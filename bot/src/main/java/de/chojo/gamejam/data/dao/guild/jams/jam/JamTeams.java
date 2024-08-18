/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam;

import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class JamTeams {
    private final Jam jam;

    public JamTeams(Jam jam) {
        this.jam = jam;
    }

    public Team create(String name) {
        var teamId = query("""
                INSERT INTO team(jam_id) VALUES(?) RETURNING id AS team_id;
                """)
                .single(call().bind(jam.jamId()))
                .map(row -> row.getInt("team_id"))
                .first()
                .orElseThrow();
        query("""
                INSERT INTO team_meta(team_id, team_name) VALUES (?,?);
                """)
                .single(call().bind(teamId).bind(name))
                .insert();
        return new Team(jam, teamId);
    }

    public List<Team> teams() {
        return query("""
                SELECT id,
                       team_name
                FROM team t
                   LEFT JOIN team_meta m ON t.id = m.team_id
                WHERE jam_id = ?
                """)
                .single(call().bind(jam.jamId()))
                .map(r -> new Team(jam, r.getInt("id")))
                .all();
    }

    public Optional<Team> byMember(Member member) {
        return byMember(member.getUser());
    }

    public Optional<Team> byMember(User member) {
        return query("""
                SELECT m.team_id
                FROM team_member m
                    LEFT JOIN team t ON t.id = m.team_id
                    LEFT JOIN jam j ON j.id = t.jam_id
                WHERE j.id = ?
                    AND user_id = ?
                """)
                .single(call().bind(jam.jamId()).bind(member.getIdLong()))
                .map(r -> r.getInt("team_id"))
                .first()
                .flatMap(this::byId);
    }

    public Optional<Team> byName(String name) {
        return query("""
                SELECT id FROM team t LEFT JOIN team_meta m ON t.id = m.team_id
                WHERE jam_id = ?
                    AND LOWER(m.team_name) = LOWER(?)
                """)
                .single(call().bind(jam.jamId()).bind(name))
                .map(r -> r.getInt("id"))
                .first()
                .flatMap(this::byId);
    }

    public Optional<Team> byId(int id) {
        return query("SELECT id, team_name FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .single(call().bind(id))
                .map(r -> new Team(jam, r.getInt("id")))
                .first();
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
