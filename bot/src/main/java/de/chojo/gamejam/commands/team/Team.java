/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.team.handler.Create;
import de.chojo.gamejam.commands.team.handler.Disband;
import de.chojo.gamejam.commands.team.handler.Invite;
import de.chojo.gamejam.commands.team.handler.Leave;
import de.chojo.gamejam.commands.team.handler.List;
import de.chojo.gamejam.commands.team.handler.Profile;
import de.chojo.gamejam.commands.team.handler.Promote;
import de.chojo.gamejam.commands.team.handler.Rename;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Team extends SlashCommand {
    public Team(TeamData teamData, JamData jamData) {
        super(Slash.of("team", "command.team.description")
                .subCommand(SubCommand.of("create", "command.team.create.description")
                        .handler(new Create(teamData, jamData))
                        .argument(Argument.text("name", "command.team.create.name.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("invite", "command.team.invite.description")
                        .handler(new Invite(teamData, jamData))
                        .argument(Argument.user("user", "command.team.invite.user.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("leave", "command.team.leave.description")
                        .handler(new Leave(teamData, jamData)))
                .subCommand(SubCommand.of("disband", "command.team.disband.description")
                        .handler(new Disband(teamData, jamData))
                        .argument(Argument.bool("confirm", "command.team.disband.confirm.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("promote", "command.team.promote.description")
                        .handler(new Promote(teamData, jamData))
                        .argument(Argument.user("user", "command.team.promote.user.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("profile", "command.team.profile.description")
                        .handler(new Profile(teamData, jamData))
                        .argument(Argument.user("user", "command.team.profile.user.description"))
                        .argument(Argument.text("team", "command.team.profile.team.description")
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("list", "command.team.list.description")
                        .handler(new List(teamData, jamData)))
                .subCommand(SubCommand.of("rename", "command.team.rename.description")
                        .handler(new Rename(teamData, jamData))
                        .argument(Argument.text("name", "command.team.rename.name.description")
                                          .asRequired()))
        );
    }
}
