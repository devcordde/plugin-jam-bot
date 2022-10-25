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

import java.util.List;
import java.util.Optional;

public class JamTeams extends QueryFactory {
    private final Jam jam;

    public JamTeams(Jam jam) {
        super(jam);
        this.jam = jam;
    }

    public Team create(String name) {
        return builder(Team.class)
                .query("""
                       WITH id AS (
                           INSERT INTO team(jam_id) VALUES(?) RETURNING id AS team_id
                       )
                       INSERT INTO team_meta(team_id, name)
                       VALUES ((SELECT team_id FROM id),?) RETURNING team_id;
                       """)
                .parameter(stmt -> stmt.setInt(jam.jamId()).setString(name))
                .readRow(row -> new Team(jam, row.getInt("id")))
                .firstSync()
                .orElseThrow();
    }

    public List<Team> teams() {
        return builder(Team.class)
                .query("""
                       SELECT id,
                              name
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
                       SELECT
                           m.team_id
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
                           AND LOWER(m.name) = LOWER(?)
                       """)
                .parameter(p -> p.setInt(jam.jamId()).setString(name))
                .readRow(r -> r.getInt("id"))
                .firstSync()
                .flatMap(this::byId);
    }

    public Optional<Team> byId(int id) {
        return builder(Team.class)
                .query("SELECT id, name FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .readRow(r -> new Team(jam, r.getInt("id")))
                .firstSync();
    }
}
