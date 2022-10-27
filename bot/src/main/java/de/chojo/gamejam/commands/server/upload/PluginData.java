/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.upload;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.commands.server.util.ProgressDownloader;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class PluginData implements SlashHandler {
    private static final Logger log = getLogger(PluginData.class);
    private final Server server;
    private final Guilds guilds;
    private final ServerService serverService;

    public PluginData(Server server, Guilds guilds, ServerService serverService) {
        this.server = server;
        this.guilds = guilds;
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;
        var teamServer = optServer.get();

        var downloadUrl = event.getOption("file").getAsAttachment().getProxy().getUrl();
        var path = event.getOption("path").getAsString();

        if (path.contains("..")) {
            event.reply("Invalid path").queue();
            return;
        }

        var download = ProgressDownloader.download(event, context, downloadUrl);

        if (download.isEmpty()) return;

        var pluginFile = teamServer.plugins().resolve(path);
        // No upload in plugin root
        if (pluginFile.getParent().equals(teamServer.plugins())) {
            event.reply("Invalid path").queue();
            return;
        }

        // No updates into the update directory
        if(pluginFile.equals(teamServer.plugins().resolve("update"))){
            event.reply("Invalid path").queue();
            return;
        }
        try {
            Files.copy(download.get(), pluginFile, StandardCopyOption.REPLACE_EXISTING);
            event.getHook().editOriginal("Added or replaced file.").queue();
        } catch (IOException e) {
            event.getHook().editOriginal("Failed to add file.").queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var optServer = guilds.guild(event).jams().activeJam()
                              .map(Jam::teams)
                              .flatMap(teams -> teams.byMember(event.getUser()))
                              .map(serverService::get);
        if (optServer.isEmpty()) return;
        var server = optServer.get();

        var option = event.getFocusedOption();
        if ("path".equals(option.getName())) {
            var plugins = server.plugins();
            var currPath = option.getValue();
            if (currPath.contains("..")) {
                event.replyChoices().queue();
                return;
            }
            var split = currPath.split("/");

            // Root dir
            if (split.length == 1 && !currPath.endsWith("/")) {
                var currValue = split[0].toLowerCase();
                var files = files(plugins).stream()
                                          .filter(File::isDirectory)
                                          .filter(file -> file.getName().toLowerCase().startsWith(currValue))
                                          .limit(25)
                                          .map(line -> fileName(plugins, line));
                event.replyChoices(Choice.toStringChoice(files)).queue();
                return;
            }

            if (!currPath.endsWith("/")) {
                split = Arrays.copyOfRange(split, 0, split.length - 1);
            }

            var path = plugins;

            for (var s : split) {
                path = path.resolve(s);
            }

            if (currPath.endsWith("/")) {
                var files = files(path).stream()
                                       .limit(25)
                                       .map(line -> fileName(plugins, line));
                event.replyChoices(Choice.toStringChoice(files)).queue();
                return;
            }
            split = currPath.split("/");
            var currValue = currPath.split("/")[split.length - 1].toLowerCase();

            var files = files(path).stream()
                                   .filter(file -> file.getName().toLowerCase().startsWith(currValue))
                                   .limit(24)
                                   .map(line -> fileName(plugins, line))
                                   .collect(Collectors.toCollection(ArrayList::new));

            files.add(0, currPath);

            event.replyChoices(Choice.toStringChoice(files)).queue();
        }
    }

    private List<File> files(Path path) {
        return Arrays.stream(path.toFile().listFiles()).toList();
    }

    private String fileName(Path strip, File file) {
        if (file.isDirectory()) {
            return (file.toPath() + "/").replace(strip.toString(), "").replaceAll("^/", "");
        }
        return file.toPath().toString().replace(strip.toString(), "").replaceAll("^/", "");

    }
}
