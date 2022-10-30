/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server;

import de.chojo.gamejam.commands.server.configure.MaxPlayers;
import de.chojo.gamejam.commands.server.configure.Message;
import de.chojo.gamejam.commands.server.configure.SpectatorOverflow;
import de.chojo.gamejam.commands.server.configure.Whitelist;
import de.chojo.gamejam.commands.server.download.DownloadPluginData;
import de.chojo.gamejam.commands.server.plugins.Install;
import de.chojo.gamejam.commands.server.plugins.Uninstall;
import de.chojo.gamejam.commands.server.process.Console;
import de.chojo.gamejam.commands.server.process.Log;
import de.chojo.gamejam.commands.server.process.Restart;
import de.chojo.gamejam.commands.server.process.Start;
import de.chojo.gamejam.commands.server.process.Status;
import de.chojo.gamejam.commands.server.process.Stop;
import de.chojo.gamejam.commands.server.system.Delete;
import de.chojo.gamejam.commands.server.system.Setup;
import de.chojo.gamejam.commands.server.upload.Plugin;
import de.chojo.gamejam.commands.server.upload.UploadPluginData;
import de.chojo.gamejam.commands.server.upload.World;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.TeamServer;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class Server implements SlashProvider<Slash> {
    private final Guilds guilds;
    private final ServerService serverService;
    private final Configuration configuration;

    public Server(Guilds guilds, ServerService serverService, Configuration configuration) {
        this.guilds = guilds;
        this.serverService = serverService;
        this.configuration = configuration;
    }

    @Override
    public Slash slash() {
        return Slash.of("server", "Manage your server")
                .adminCommand()
                .group(Group.of("process", "Manage server process")
                        .subCommand(SubCommand.of("start", "Start the server")
                                .handler(new Start(this)))
                        .subCommand(SubCommand.of("stop", "Stop the server")
                                .handler(new Stop(this)))
                        .subCommand(SubCommand.of("restart", "Restart the server")
                                .handler(new Restart(this)))
                        .subCommand(SubCommand.of("console", "Send a command via console")
                                .handler(new Console(this))
                                .argument(Argument.text("command", "The command to send").asRequired()))
                        .subCommand(SubCommand.of("status", "Server status")
                                .handler(new Status(this)))
                        .subCommand(SubCommand.of("log", "Restart the server")
                                .handler(new Log(this))))
                .group(Group.of("system", "manage the server system")
                        .subCommand(SubCommand.of("setup", "Setup the server")
                                .handler(new Setup(this)))
                        .subCommand(SubCommand.of("delete", "Delete the server data")
                                .handler(new Delete(this))))
                .group(Group.of("upload", "Upload files")
                        .subCommand(SubCommand.of("world", "Upload a world replacing the current world")
                                .handler(new World(this))
                                .argument(Argument.text("url", "Link to download the world as zip"))
                                .argument(Argument.attachment("file", "World as zip")))
                        .subCommand(SubCommand.of("plugin", "Upload your plugin")
                                .handler(new Plugin(this))
                                .argument(Argument.attachment("file", "Your plugin file")
                                                  .asRequired()))
                        .subCommand(SubCommand.of("plugindata", "Upload plugin data")
                                .handler(new UploadPluginData(this, guilds, serverService))
                                .argument(Argument.text("path", "Path in the plugin directory")
                                                  .asRequired()
                                                  .withAutoComplete())
                                .argument(Argument.attachment("file", "File to upload")
                                                  .asRequired())))
                .group(Group.of("download", "Download files")
                        .subCommand(SubCommand.of("plugindata", "Download plugin files")
                                .handler(new DownloadPluginData(this, guilds, serverService))
                                .argument(Argument.text("path", "Path in the plugin directory")
                                                  .asRequired()
                                                  .withAutoComplete())))
                .group(Group.of("plugins", "Manage installed plugins")
                        .subCommand(SubCommand.of("install", "install another plugin")
                                .handler(new Install(configuration, this))
                                .argument(Argument.text("plugin", "Plugin to install")
                                                  .asRequired()
                                                  .withAutoComplete()))
                        .subCommand(SubCommand.of("uninstall", "Plugin to uninstall")
                                .handler(new Uninstall(this, configuration, guilds, serverService))
                                .argument(Argument.text("plugin", "The plugin to uninstall").asRequired()
                                                  .withAutoComplete())
                                .argument(Argument.bool("deletedata", "True to delete the plugin data as well")
                                                  .asRequired())))
                .group(Group.of("configure", "Configure server")
                        .subCommand(SubCommand.of("message", "Set the welcome message")
                                .handler(new Message(this)))
                        .subCommand(SubCommand.of("maxplayers", "Set the max players of this server")
                                .handler(new MaxPlayers(this))
                                .argument(Argument.integer("amount", "Max amount of players.").asRequired()))
                        .subCommand(SubCommand.of("spectatoroverflow", "Active the spectator overflow")
                                .handler(new SpectatorOverflow(this))
                                .argument(Argument.bool("state", "true to enable overflow").asRequired()))
                        .subCommand(SubCommand.of("whitelist", "Enable the whitelist")
                                .handler(new Whitelist(this))
                                .argument(Argument.bool("state", "true to enable whitelist").asRequired())))
                .build();
    }

    public Optional<TeamServer> getServer(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = guilds.guild(event).jams().activeJam();

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return Optional.empty();
        }

        var jam = optJam.get();
        var optTeam = jam.teams().byMember(event.getUser());

        if (optTeam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return Optional.empty();
        }

        return Optional.ofNullable(serverService.get(optTeam.get()));
    }
}
