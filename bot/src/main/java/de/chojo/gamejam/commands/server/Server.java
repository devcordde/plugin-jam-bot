package de.chojo.gamejam.commands.server;

import de.chojo.gamejam.configuration.elements.ServerManagement;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Server extends SlashCommand {
    public Server(ServerService serverService) {
        super(Slash.of("server", "Manage your server")
                .group(Group.of("process", "Manage server process")
                        .subCommand(SubCommand.of("start", "Start the server")
                                .handler(null))
                        .subCommand(SubCommand.of("stop", "Stop the server")
                                .handler(null))
                        .subCommand(SubCommand.of("restart", "Restart the server")
                                .handler(null))
                )
                .group(Group.of("system", "manage the server system")
                        .subCommand(SubCommand.of("setup", "Setup the server")
                                .handler(null))
                        .subCommand(SubCommand.of("delete", "Delete the server data")
                                .handler(null)))
                .group(Group.of("upload", "Upload files")
                        .subCommand(SubCommand.of("world", "Upload a world replacing the current world")
                                .handler(null)
                                .argument(Argument.text("download", "link to download the world"))
                                .argument(Argument.attachment("file", "World as zip")))
                        .subCommand(SubCommand.of("plugin", "Upload your plugin")
                                .handler(null)
                                .argument(Argument.attachment("file", "Your plugin file")
                                                  .asRequired()))
                        .subCommand(SubCommand.of("plugindata", "Upload plugin data")
                                .handler(null)
                                .argument(Argument.text("path", "Path in the plugin directory")
                                                  .asRequired()
                                                  .withAutoComplete())
                                .argument(Argument.attachment("file", "File to upload")
                                                  .asRequired()))
                )
                .group(Group.of("plugins", "Manage installed plugins")
                        .subCommand(SubCommand.of("install", "install another plugin")
                                .handler(null)
                                .argument(Argument.text("plugin", "Plugin to install")
                                                  .asRequired()
                                                  .withAutoComplete())
                        )
                        .subCommand(SubCommand.of("uninstall", "Plugin to uninstall")
                                .handler(null)
                                .argument(Argument.bool("deletedata", "True to delete the plugin data as well")))
                )
        );
    }
}
