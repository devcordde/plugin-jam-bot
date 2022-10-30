/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1.wrapper;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;

public record TeamProfile(int id, String name, String description, long leaderId, String projectUrl) {

    public static TeamProfile build(Team team) {
        return new TeamProfile(team.id(), team.meta().name(), "Cool description", team.meta().leader(), "https://github.com/devcordde/plugin-jam-bot");
    }
}
