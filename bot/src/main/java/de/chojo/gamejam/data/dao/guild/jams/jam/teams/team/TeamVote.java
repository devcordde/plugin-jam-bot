/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.teams.team;


import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;

public record TeamVote(Team team, int rank, int votes) {
}
