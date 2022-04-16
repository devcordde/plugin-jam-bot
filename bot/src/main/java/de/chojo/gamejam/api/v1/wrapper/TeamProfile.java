/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1.wrapper;

import de.chojo.gamejam.data.wrapper.team.JamTeam;

import java.util.List;

public record TeamProfile(int id, String name, String description, long leaderId, String githubUrl) {

    public static TeamProfile build(JamTeam team) {
        return new TeamProfile(team.id(), team.name(), "Cool description", team.leader(), "https://github.com/devcordde/plugin-jam-bot");
    }
}
