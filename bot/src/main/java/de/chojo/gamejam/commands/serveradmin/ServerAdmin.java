/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.serveradmin;

import de.chojo.gamejam.commands.serveradmin.handler.SyncVelocity;
import de.chojo.gamejam.commands.serveradmin.handler.info.Detailed;
import de.chojo.gamejam.commands.serveradmin.handler.info.Short;
import de.chojo.gamejam.commands.serveradmin.handler.refresh.RefreshAll;
import de.chojo.gamejam.commands.serveradmin.handler.restart.RestartAll;
import de.chojo.gamejam.commands.serveradmin.handler.restart.RestartTeam;
import de.chojo.gamejam.commands.serveradmin.handler.start.StartAll;
import de.chojo.gamejam.commands.serveradmin.handler.start.StartTeam;
import de.chojo.gamejam.commands.serveradmin.handler.stop.StopAll;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class ServerAdmin extends SlashCommand {
    public ServerAdmin(Guilds guilds, ServerService serverService) {
        super(Slash.of("serveradmin", "command.serveradmin.description")
                .adminCommand()
                .group(Group.of("start", "command.serveradmin.start.description")
                        .subCommand(SubCommand.of("all", "command.serveradmin.start.all.description")
                                .handler(new StartAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "command.serveradmin.start.team.description")
                                .handler(new StartTeam(serverService, guilds))
                                .argument(Argument.text("team", "command.serveradmin.start.team.options.team.description").asRequired().withAutoComplete())))
                .group(Group.of("restart", "command.serveradmin.restart.description")
                        .subCommand(SubCommand.of("all", "command.serveradmin.restart.all.description")
                                .handler(new RestartAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "command.serveradmin.restart.team.description")
                                .handler(new RestartTeam(serverService, guilds))
                                .argument(Argument.text("team", "command.serveradmin.restart.team.options.team.description").asRequired().withAutoComplete())))
                .group(Group.of("stop", "command.serveradmin.stop.description")
                        .subCommand(SubCommand.of("all", "command.serveradmin.stop.all.description")
                                .handler(new StopAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "command.serveradmin.stop.team.description")
                                .handler(new StopAll(serverService, guilds))
                                .argument(Argument.text("team", "command.serveradmin.stop.team.options.team.description").asRequired().withAutoComplete())))
                .group(Group.of("refresh", "command.serveradmin.refresh.description")
                        .subCommand(SubCommand.of("all", "command.serveradmin.refresh.all.description")
                                .handler(new RefreshAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "command.serveradmin.refresh.team.description")
                                .handler(new RefreshAll(serverService, guilds))
                                .argument(Argument.text("team", "command.serveradmin.refresh.team.options.team.description").asRequired().withAutoComplete())))
                .group(Group.of("info", "command.serveradmin.info.description")
                        .subCommand(SubCommand.of("short", "command.serveradmin.info.short.description")
                                .handler(new Short(serverService, guilds)))
                        .subCommand(SubCommand.of("detailed", "command.serveradmin.info.detailed.description")
                                .handler(new Detailed(serverService, guilds))
                                .argument(Argument.text("team", "command.serveradmin.info.detailed.options.team.description").withAutoComplete())))
                .subCommand(SubCommand.of("syncvelocity", "command.serveradmin.syncvelocity.description")
                        .handler(new SyncVelocity(serverService)))
        );
    }
}
