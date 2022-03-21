/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.commands.team.Create;
import de.chojo.gamejam.commands.team.Disband;
import de.chojo.gamejam.commands.team.Invite;
import de.chojo.gamejam.commands.team.Leave;
import de.chojo.gamejam.commands.team.Profile;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public class Team extends SimpleCommand {
    private final JamData jamData;

    private final Map<String, SubCommand<Jam>> subcommands = new HashMap<>();

    public Team(TeamData teamData, JamData jamData) {
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
                .addSubCommand("disband", "Disband your team",
                        argsBuilder().add(SimpleArgument.bool("confirm", "confirm with true").asRequired()).build())
//                .addSubCommand("new-leader", "Pass team leadership to someone else",
//                        argsBuilder()
//                                .add(SimpleArgument.user("new_leader", "The new leader").asRequired())
//                                .build())
                .addSubCommand("profile", "Shows the profile of a team or your own",
                        argsBuilder()
                                .add(SimpleArgument.user("user", "Show the team of the user"))
                                .add(SimpleArgument.string("team", "Show the team profile").withAutoComplete())
                                .build())
                .build());
        this.jamData = jamData;
        this.subcommands.put("create", new Create(teamData));
        this.subcommands.put("invite", new Invite(teamData, jamData));
        this.subcommands.put("leave", new Leave(teamData));
        this.subcommands.put("disband", new Disband(teamData));
        this.subcommands.put("profile", new Profile(teamData));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getNextOrCurrentJam(event.getGuild())
                .thenAccept(optJam -> {
                    if (optJam.isEmpty()) {
                        event.reply("No jam is in progress. Teams are not available.").setEphemeral(true).queue();
                        return;
                    }

                    var subCommand = subcommands.get(event.getSubcommandName());

                    if (subCommand != null) {
                        subCommand.execute(event, context, optJam.get());
                    }
                }).whenComplete(Future.error());
    }
}
