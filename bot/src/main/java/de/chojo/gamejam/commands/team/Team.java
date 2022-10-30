/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.team.handler.Create;
import de.chojo.gamejam.commands.team.handler.Disband;
import de.chojo.gamejam.commands.team.handler.Edit;
import de.chojo.gamejam.commands.team.handler.Invite;
import de.chojo.gamejam.commands.team.handler.Leave;
import de.chojo.gamejam.commands.team.handler.List;
import de.chojo.gamejam.commands.team.handler.Profile;
import de.chojo.gamejam.commands.team.handler.Promote;
import de.chojo.gamejam.commands.team.handler.Rename;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Team extends SlashCommand {
    public Team(Guilds guilds) {
        super(Slash.of("team", "command.team.description")
                .subCommand(SubCommand.of("create", "command.team.create.description")
                        .handler(new Create(guilds))
                        .argument(Argument.text("name", "command.team.create.options.name.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("edit", "Edit Eeam information")
                        .handler(new Edit(guilds)))
                .subCommand(SubCommand.of("invite", "command.team.invite.description")
                        .handler(new Invite(guilds))
                        .argument(Argument.user("user", "command.team.invite.options.user.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("leave", "command.team.leave.description")
                        .handler(new Leave(guilds)))
                .subCommand(SubCommand.of("disband", "command.team.disband.description")
                        .handler(new Disband(guilds))
                        .argument(Argument.bool("confirm", "command.team.disband.options.confirm.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("promote", "command.team.promote.description")
                        .handler(new Promote(guilds))
                        .argument(Argument.user("user", "command.team.promote.options.user.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("profile", "command.team.profile.description")
                        .handler(new Profile(guilds))
                        .argument(Argument.user("user", "command.team.profile.options.user.description"))
                        .argument(Argument.text("team", "command.team.profile.options.team.description")
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("list", "command.team.list.description")
                        .handler(new List(guilds)))
                .subCommand(SubCommand.of("rename", "command.team.rename.description")
                        .handler(new Rename(guilds))
                        .argument(Argument.text("name", "command.team.rename.options.name.description")
                                          .asRequired()))
        );
    }
}
