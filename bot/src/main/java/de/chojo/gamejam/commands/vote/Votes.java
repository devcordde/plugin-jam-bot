/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote;

import de.chojo.gamejam.commands.vote.handler.Info;
import de.chojo.gamejam.commands.vote.handler.Ranking;
import de.chojo.gamejam.commands.vote.handler.Vote;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Votes extends SlashCommand {
    public Votes(Guilds guilds) {
        super(Slash.of("votes", "command.votes.description")
                .subCommand(SubCommand.of("vote", "command.votes.vote.description")
                        .handler(new Vote(guilds))
                        .argument(Argument.text("team", "command.votes.vote.team.description").asRequired()
                                          .withAutoComplete())
                        .argument(Argument.integer("points", "command.votes.vote.points.description").asRequired()
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("info", "command.votes.info.description")
                        .handler(new Info(guilds)))
                .subCommand(SubCommand.of("ranking", "command.votes.ranking.description")
                        .handler(new Ranking(guilds)))
        );
    }
}
