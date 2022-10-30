/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.votes;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;

public record VoteEntry(Team team, long voterId, int points) {
}
