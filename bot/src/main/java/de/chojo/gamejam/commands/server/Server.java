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
        return Slash.of("server", "command.server.description")
                .adminCommand()
                .group(Group.of("process", "command.server.process.description")
                        .subCommand(SubCommand.of("start", "command.server.process.start.description")
                                .handler(new Start(this)))
                        .subCommand(SubCommand.of("stop", "command.server.process.stop.description")
                                .handler(new Stop(this)))
                        .subCommand(SubCommand.of("restart", "command.server.process.restart.description")
                                .handler(new Restart(this)))
                        .subCommand(SubCommand.of("console", "command.server.process.console.description")
                                .handler(new Console(this))
                                .argument(Argument.text("command", "command.server.process.console.command.description").asRequired()))
                        .subCommand(SubCommand.of("status", "command.server.process.status.description")
                                .handler(new Status(this)))
                        .subCommand(SubCommand.of("log", "command.server.process.log.description")
                                .handler(new Log(this))))
                .group(Group.of("system", "command.server.system.description")
                        .subCommand(SubCommand.of("setup", "command.server.system.setup.description")
                                .handler(new Setup(this)))
                        .subCommand(SubCommand.of("delete", "command.server.system.delete.description")
                                .handler(new Delete(this))))
                .group(Group.of("upload", "command.server.upload.description")
                        .subCommand(SubCommand.of("world", "command.server.upload.world.description")
                                .handler(new World(this))
                                .argument(Argument.text("url", "command.server.upload.world.url.description"))
                                .argument(Argument.attachment("file", "command.server.upload.world.file.description")))
                        .subCommand(SubCommand.of("plugin", "command.server.upload.plugin.description")
                                .handler(new Plugin(this))
                                .argument(Argument.attachment("file", "command.server.upload.plugin.file.description")
                                                  .asRequired()))
                        .subCommand(SubCommand.of("plugindata", "command.server.upload.plugindata.description")
                                .handler(new UploadPluginData(this, guilds, serverService))
                                .argument(Argument.text("path", "command.server.upload.plugindata.path.description")
                                                  .asRequired()
                                                  .withAutoComplete())
                                .argument(Argument.attachment("file", "command.server.upload.plugindata.file.description")
                                                  .asRequired())))
                .group(Group.of("download", "command.server.download.description")
                        .subCommand(SubCommand.of("plugindata", "command.server.download.plugindata.description")
                                .handler(new DownloadPluginData(this, guilds, serverService))
                                .argument(Argument.text("path", "command.server.download.plugindata.path.description")
                                                  .asRequired()
                                                  .withAutoComplete())))
                .group(Group.of("plugins", "command.server.plugins.description")
                        .subCommand(SubCommand.of("install", "command.server.plugins.install.description")
                                .handler(new Install(configuration, this))
                                .argument(Argument.text("plugin", "command.server.plugins.install.plugin.description")
                                                  .asRequired()
                                                  .withAutoComplete()))
                        .subCommand(SubCommand.of("uninstall", "command.server.plugins.uninstall.description")
                                .handler(new Uninstall(this, configuration, guilds, serverService))
                                .argument(Argument.text("plugin", "command.server.plugins.uninstall.plugin.description").asRequired()
                                                  .withAutoComplete())
                                .argument(Argument.bool("deletedata", "command.server.plugins.uninstall.deletedata.description")
                                                  .asRequired())))
                .group(Group.of("configure", "command.server.configure.description")
                        .subCommand(SubCommand.of("message", "command.server.configure.message.description")
                                .handler(new Message(this)))
                        .subCommand(SubCommand.of("maxplayers", "command.server.configure.maxplayers.description")
                                .handler(new MaxPlayers(this))
                                .argument(Argument.integer("amount", "command.server.configure.maxplayers.amount.description").asRequired()))
                        .subCommand(SubCommand.of("spectatoroverflow", "command.server.configure.spectatoroverflow.description")
                                .handler(new SpectatorOverflow(this))
                                .argument(Argument.bool("state", "command.server.configure.spectatoroverflow.state.description").asRequired()))
                        .subCommand(SubCommand.of("whitelist", "command.server.configure.whitelist.description")
                                .handler(new Whitelist(this))
                                .argument(Argument.bool("state", "command.server.configure.whitelist.state.description").asRequired())))
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
