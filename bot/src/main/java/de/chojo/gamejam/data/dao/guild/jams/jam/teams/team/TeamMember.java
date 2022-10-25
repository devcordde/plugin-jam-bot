/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.teams.team;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;

public final class TeamMember extends QueryFactory {
    private final Team team;
    private final long userId;

    public TeamMember(Team team, long userId) {
        super(team);
        this.team = team;
        this.userId = userId;
    }

    public Team team() {
        return team;
    }

    public long userId() {
        return userId;
    }

    public boolean leave() {
        return builder()
                .query("DELETE FROM team_member WHERE team_id = ? AND user_id = ?")
                .parameter(p -> p.setInt(team.id()).setLong(userId))
                .insert()
                .sendSync()
                .changed();
    }
}
