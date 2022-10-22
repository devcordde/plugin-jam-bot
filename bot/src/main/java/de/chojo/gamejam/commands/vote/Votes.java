/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote;

import de.chojo.gamejam.commands.vote.handler.Info;
import de.chojo.gamejam.commands.vote.handler.Ranking;
import de.chojo.gamejam.commands.vote.handler.Vote;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Votes extends SlashCommand {

    public Votes(JamData jamData, TeamData teamData) {
        super(Slash.of("votes", "Vote for teams")
                .subCommand(SubCommand.of("vote", "vote for teams")
                        .handler(new Vote(teamData, jamData))
                        .argument(Argument.text("team", "Name of the team").asRequired().withAutoComplete())
                        .argument(Argument.integer("points", "Points to give.").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("info", "Information about your given vote")
                        .handler(new Info(teamData, jamData)))
                .subCommand(SubCommand.of("ranking", "The current ranking")
                        .handler(new Ranking(teamData, jamData)))
        );
    }
}
