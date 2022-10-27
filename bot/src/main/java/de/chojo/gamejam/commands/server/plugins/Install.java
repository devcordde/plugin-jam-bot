/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.plugins;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

import static org.slf4j.LoggerFactory.getLogger;

public class Install implements SlashHandler {
    private static final Logger log = getLogger(Install.class);
    private final Configuration configuration;
    private final Server server;

    public Install(Configuration configuration, Server server) {
        this.configuration = configuration;
        this.server = server;
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
        try {
            Files.createSymbolicLink(pluginPath, plugin.toAbsolutePath());
        } catch (IOException e) {
            event.reply("Could not install plugin").queue();
            log.error("Could not install plugin", e);
            return;
        }
        event.reply("Installed " + pluginName + ". Restart to apply changes").queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("plugin".equals(option.getName())) {
            var currValue = option.getValue().toLowerCase();
            if (currValue.isEmpty()) {
                event.replyChoices(Choice.toStringChoice(configuration.plugins().pluginNames().stream().limit(25)))
                     .queue();
                return;
            }
            var stream = configuration.plugins().pluginNames().stream()
                                      .filter(name -> name.toLowerCase().startsWith(currValue))
                                      .limit(25);
            event.replyChoices(Choice.toStringChoice(stream)).queue();
        }
    }
}
