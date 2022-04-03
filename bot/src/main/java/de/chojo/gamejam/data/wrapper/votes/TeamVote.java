/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.votes;


import de.chojo.gamejam.data.wrapper.team.JamTeam;

public record TeamVote(JamTeam jamTeam, int rank, int votes) {
}
