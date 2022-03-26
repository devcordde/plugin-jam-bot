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
import de.chojo.jdautil.util.MapBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Map;

public class Team extends SimpleCommand {
    private final JamData jamData;

    private final Map<String, SubCommand<Jam>> subcommands;

    public Team(TeamData teamData, JamData jamData) {
        super(CommandMeta.builder("team", "command.team.description")
                .addSubCommand("create", "command.team.create.description",
                        argsBuilder()
                                .add(SimpleArgument.string("name", "command.team.create.arg.name").asRequired())
                                .build())
                .addSubCommand("invite", "command.team.invite.description",
                        argsBuilder()
                                .add(SimpleArgument.user("user", "command.team.invite.arg.user").asRequired())
                                .build())
                .addSubCommand("leave", "command.team.leave.description")
                .addSubCommand("disband", "command.team.disband.description",
                        argsBuilder().add(SimpleArgument.bool("confirm", "command.team.disband.arg.confirm").asRequired()).build())
//                .addSubCommand("new-leader", "Pass team leadership to someone else",
//                        argsBuilder()
//                                .add(SimpleArgument.user("new_leader", "The new leader").asRequired())
//                                .build())
                .addSubCommand("profile", "command.team.profile.description",
                        argsBuilder()
                                .add(SimpleArgument.user("user", "command.team.profile.arg.user"))
                                .add(SimpleArgument.string("team", "command.team.profile.arg.team").withAutoComplete())
                                .build())
                .build());
        this.jamData = jamData;
        subcommands = new MapBuilder<String, SubCommand<Jam>>()
                .add("create", new Create(teamData))
                .add("invite", new Invite(teamData, jamData))
                .add("leave", new Leave(teamData))
                .add("disband", new Disband(teamData))
                .add("profile", new Profile(teamData))
                .build();
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getNextOrCurrentJam(event.getGuild())
                .thenAccept(optJam -> {
                    if (optJam.isEmpty()) {
                        event.reply(context.localize("command.team.noJamActive")).setEphemeral(true).queue();
                        return;
                    }

                    var subCommand = subcommands.get(event.getSubcommandName());

                    if (subCommand != null) {
                        subCommand.execute(event, context, optJam.get());
                    }
                }).whenComplete(Future.error());
    }
}
