/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.upload;

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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final Guilds guilds;
    private final ServerService serverService;

    public PluginData(Guilds guilds, ServerService serverService) {
        this.guilds = guilds;
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = guilds.guild(event).jams().activeJam();

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }

        var jam = optJam.get();
        var optTeam = jam.teams().byMember(event.getUser());

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        var team = optTeam.get();

        var teamServer = serverService.get(team);


        var downloadUrl = event.getOption("file").getAsAttachment().getProxy().getUrl();

        event.reply("Attempting to download file").queue();

        Path file;
        try {
            file = Files.createTempFile("upload", String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            log.error("Failed to create download file", e);
            event.getHook().editOriginal("Failed to create download file").queue();
            return;
        }

        var request = HttpRequest.newBuilder(URI.create(downloadUrl)).GET().build();

        try {
            event.getHook().editOriginal("Downloading file.").queue();
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(file));
        } catch (IOException e) {
            log.error("Failed to write response", e);
            event.getHook().editOriginal("Could not download file.").queue();
            return;
        } catch (InterruptedException e) {
            log.error("Failed to retrieve response", e);
            event.getHook().editOriginal("Could not download file.").queue();
            return;
        }

        event.getHook().editOriginal("Download done. Replacing.").queue();
        var pluginFile = teamServer.plugins();
        pluginFile = pluginFile.resolve(event.getOption("path").getAsString());
        try {
            Files.copy(file, pluginFile, StandardCopyOption.REPLACE_EXISTING);
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
