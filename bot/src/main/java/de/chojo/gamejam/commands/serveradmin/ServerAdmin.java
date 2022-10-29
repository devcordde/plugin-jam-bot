/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.serveradmin;

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
        super(Slash.of("serveradmin", "Administration of team servers")
                .adminCommand()
                .group(Group.of("start", "Start servers")
                        .subCommand(SubCommand.of("all", "Start all server")
                                .handler(new StartAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "Start a team server")
                                .handler(new StartTeam(serverService, guilds))
                                .argument(Argument.text("team", "team").asRequired().withAutoComplete())))
                .group(Group.of("restart", "Restart servers")
                        .subCommand(SubCommand.of("all", "Start all server")
                                .handler(new RestartAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "Start a team server")
                                .handler(new RestartTeam(serverService, guilds))
                                .argument(Argument.text("team", "team").asRequired().withAutoComplete())))
                .group(Group.of("stop", "Stop servers")
                        .subCommand(SubCommand.of("all", "Stop all server")
                                .handler(new StopAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "Stop a team server")
                                .handler(new StopAll(serverService, guilds))
                                .argument(Argument.text("team", "team").asRequired().withAutoComplete())))
                .group(Group.of("refresh", "Refresh files of the template in all servers.")
                        .subCommand(SubCommand.of("all", "Refresh all server")
                                .handler(new RefreshAll(serverService, guilds)))
                        .subCommand(SubCommand.of("team", "Refresh a team server")
                                .handler(new RefreshAll(serverService, guilds))
                                .argument(Argument.text("team", "team").asRequired().withAutoComplete())))
                .group(Group.of("info", "Server information")
                        .subCommand(SubCommand.of("short", "Short information about team servers")
                                .handler(new Short(serverService, guilds)))
                        .subCommand(SubCommand.of("detailed", "Detailed information about team servers")
                                .handler(new Detailed(serverService, guilds))
                                .argument(Argument.text("team", "team").withAutoComplete())))
        );
    }
}
