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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Team extends SimpleCommand {
    protected Team(CommandMeta meta) {
        super(CommandMeta.builder("team", "Manage your team")
                .addSubCommand("create", "Create a team",
                        argsBuilder()
                                .add(SimpleArgument.string("name", "Name of your team").asRequired())
                                .build())
                .addSubCommand("invite", "Invite someone",
                        argsBuilder()
                                .add(SimpleArgument.user("user", "The user you want to invite").asRequired())
                                .build())
                .addSubCommand("leave", "Leave your team")
                .addSubCommand("disband", "Disband your team")
                .addSubCommand("new leader", "Pass team leadership to someone else",
                        argsBuilder()
                                .add(SimpleArgument.user("new_leader", "The new leader").asRequired())
                                .build())
                .build());
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {

    }
}
