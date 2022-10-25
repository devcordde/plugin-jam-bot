/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Optional;

public class Teams extends QueryFactory {
    public Teams(JamGuild guild) {
        super(guild);
    }

    public Optional<Team> byId(int id) {
        return builder(Team.class)
                .query("SELECT id, name FROM team t LEFT JOIN team_meta m ON t.id = m.team_id WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .readRow(r -> new Team(null, r.getInt("id")))
                .firstSync();
    }
}
