/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Vote extends SimpleCommand {


    public Vote() {
        super(CommandMeta.builder("vote", "Vote for teams")
                .addSubCommand("vote", "vote for teams",
                        argsBuilder()
                                .add(SimpleArgument.string("team", "Name of the team").asRequired().withAutoComplete())
                                .build())
                .addSubCommand("info", "Information about your given vote")
                .addSubCommand("ranking", "The current ranking")
                .build());
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {

    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        super.onAutoComplete(event, slashCommandContext);
    }
}
