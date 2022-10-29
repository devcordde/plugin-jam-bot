/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;

import java.util.Optional;

public class Teams extends QueryFactory {
    private final JamGuild guild;

    public Teams(JamGuild guild) {
        super(guild);
        this.guild = guild;
    }

    public Optional<Team> byId(int id) {
        return builder(Team.class)
                .query("SELECT jam_id, id, team_name FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .readRow(r -> {
                    var team = guild.jams().byId(r.getInt("jam_id")).orElse(null);
                    return new Team(team, r.getInt("id"));
                })
                .firstSync();
    }
}
