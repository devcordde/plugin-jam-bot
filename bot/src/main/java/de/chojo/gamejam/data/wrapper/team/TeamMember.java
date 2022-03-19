/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

public class TeamMember {
    private final Team team;
    private final long userId;

    public TeamMember(Team team, long userId) {
        this.team = team;
        this.userId = userId;
    }

    public Team team() {
        return team;
    }

    public long userId() {
        return userId;
    }
}
