/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.plugins;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Uninstall implements SlashHandler {
    private final Server server;
    private final Configuration configuration;
    private final Guilds guilds;
    private final ServerService serverService;

    public Uninstall(Server server, Configuration configuration, Guilds guilds, ServerService serverService) {
        this.server = server;
        this.configuration = configuration;
        this.guilds = guilds;
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;
        var teamServer = optServer.get();

        var pluginName = event.getOption("plugin").getAsString();
        var optPlugin = configuration.plugins().byName(pluginName);
        if (optPlugin.isEmpty()) {
            event.reply("Plugin not found").queue();
            return;
        }
        var plugin = optPlugin.get();

        var pluginPath = teamServer.plugins().resolve(plugin.toFile().getName());
        pluginPath.toFile().delete();

        if (event.getOption("deletedata").getAsBoolean()) {
            var pluginDir = teamServer.plugins().resolve(pluginName);
            teamServer.deleteDirectory(pluginDir);
            event.reply("Uninstalled plugin and deleted data").queue();
        } else {
            event.reply("Uninstalled plugin.").queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var optServer = guilds.guild(event).jams().activeJam()
                              .map(Jam::teams)
                              .flatMap(teams -> teams.byMember(event.getUser()))
                              .map(serverService::get);
        if (optServer.isEmpty()) return;

        var option = event.getFocusedOption();
        if ("plugin".equals(option.getName())) {
            var allowedPlugins = configuration.plugins().pluginFiles().stream()
                                              .map(File::getName)
                                              .collect(Collectors.toSet());

            var installedPlugins = Stream.of(optServer.get().plugins().toFile().listFiles(File::isFile))
                    .filter(plugin -> allowedPlugins.contains(plugin.getName()))
                    .map(file -> file.getName().replace(".jar", ""))
                    .toList();

            var currValue = option.getValue().toLowerCase();

            if (currValue.isEmpty()) {
                event.replyChoices(Choice.toStringChoice(installedPlugins))
                     .queue();
                return;
            }

            var stream = installedPlugins.stream()
                                         .filter(name -> name.toLowerCase().startsWith(currValue))
                                         .limit(25);
            event.replyChoices(Choice.toStringChoice(stream)).queue();
        }
    }
}
