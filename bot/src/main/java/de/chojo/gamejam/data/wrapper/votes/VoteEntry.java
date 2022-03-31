package de.chojo.gamejam.data.wrapper.votes;

import de.chojo.gamejam.data.wrapper.team.JamTeam;

public record VoteEntry(JamTeam team, long voterId, int points) {
}
