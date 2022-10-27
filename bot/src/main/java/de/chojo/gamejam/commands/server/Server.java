/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server;

import de.chojo.gamejam.commands.server.process.Console;
import de.chojo.gamejam.commands.server.process.Log;
import de.chojo.gamejam.commands.server.process.Restart;
import de.chojo.gamejam.commands.server.process.Start;
import de.chojo.gamejam.commands.server.process.Stop;
import de.chojo.gamejam.commands.server.system.Delete;
import de.chojo.gamejam.commands.server.system.Setup;
import de.chojo.gamejam.commands.server.upload.Plugin;
import de.chojo.gamejam.commands.server.upload.PluginData;
import de.chojo.gamejam.commands.server.upload.World;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Server extends SlashCommand {
    public Server(Guilds guilds, ServerService serverService) {
        super(Slash.of("server", "Manage your server")
                .adminCommand()
                .group(Group.of("process", "Manage server process")
                        .subCommand(SubCommand.of("start", "Start the server")
                                .handler(new Start(guilds, serverService)))
                        .subCommand(SubCommand.of("stop", "Stop the server")
                                .handler(new Stop(guilds, serverService)))
                        .subCommand(SubCommand.of("restart", "Restart the server")
                                .handler(new Restart(guilds, serverService)))
                        .subCommand(SubCommand.of("console", "Send a command via console")
                                .handler(new Console(guilds, serverService)))
                        .subCommand(SubCommand.of("log", "Restart the server")
                                .handler(new Log(guilds, serverService))))
                .group(Group.of("system", "manage the server system")
                        .subCommand(SubCommand.of("setup", "Setup the server")
                                .handler(new Setup(guilds, serverService)))
                        .subCommand(SubCommand.of("delete", "Delete the server data")
                                .handler(new Delete(guilds, serverService))))
                .group(Group.of("upload", "Upload files")
                        .subCommand(SubCommand.of("world", "Upload a world replacing the current world")
                                .handler(new World(guilds, serverService))
                                .argument(Argument.text("url", "Link to download the world as zip"))
                                .argument(Argument.attachment("file", "World as zip")))
                        .subCommand(SubCommand.of("plugin", "Upload your plugin")
                                .handler(new Plugin(guilds, serverService))
                                .argument(Argument.attachment("file", "Your plugin file")
                                                  .asRequired()))
                        .subCommand(SubCommand.of("plugindata", "Upload plugin data")
                                .handler(new PluginData(guilds, serverService))
                                .argument(Argument.text("path", "Path in the plugin directory")
                                                  .asRequired()
                                                  .withAutoComplete())
                                .argument(Argument.attachment("file", "File to upload")
                                                  .asRequired())))
                .group(Group.of("plugins", "Manage installed plugins")
                        .subCommand(SubCommand.of("install", "install another plugin")
                                .handler(null)
                                .argument(Argument.text("plugin", "Plugin to install")
                                                  .asRequired()
                                                  .withAutoComplete()))
                        .subCommand(SubCommand.of("uninstall", "Plugin to uninstall")
                                .handler(null)
                                .argument(Argument.bool("deletedata", "True to delete the plugin data as well")))
                )
        );
    }
}
