/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;

import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Teams {
    private final JamGuild guild;

    public Teams(JamGuild guild) {
        this.guild = guild;
    }

    public Optional<Team> byId(int id) {
        return query("SELECT jam_id, id, team_name FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .single(call().bind(id))
                .map(r -> {
                    var team = guild.jams().byId(r.getInt("jam_id")).orElse(null);
                    return new Team(team, r.getInt("id"));
                }).first();
    }
}
