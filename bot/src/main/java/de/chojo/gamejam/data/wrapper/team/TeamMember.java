/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

public class TeamMember {
    private final JamTeam team;
    private final long userId;

    public TeamMember(JamTeam team, long userId) {
        this.team = team;
        this.userId = userId;
    }

    public JamTeam team() {
        return team;
    }

    public long userId() {
        return userId;
    }
}
