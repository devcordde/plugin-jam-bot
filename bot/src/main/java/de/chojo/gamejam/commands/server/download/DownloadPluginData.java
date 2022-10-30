/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.download;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.util.TempFile;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class DownloadPluginData implements SlashHandler {
    private static final Logger log = getLogger(DownloadPluginData.class);
    private final Server server;
    private final Guilds guilds;
    private final ServerService serverService;

    public DownloadPluginData(Server server, Guilds guilds, ServerService serverService) {
        this.server = server;
        this.guilds = guilds;
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;
        var teamServer = optServer.get();

        var path = event.getOption("path").getAsString();

        if (path.contains("..")) {
            event.reply(context.localize("error.invalidpath")).queue();
            return;
        }

        var pluginFile = teamServer.plugins().resolve(path);

        if (pluginFile.toFile().isFile()) {
            // No download from plugin root
            if (pluginFile.getParent().equals(teamServer.plugins())) {
                event.reply(context.localize("error.invalidpath")).queue();
                return;
            }
            event.replyFiles(FileUpload.fromData(pluginFile, pluginFile.toFile().getName())).queue();
            return;
        }

        event.reply(context.localize("command.server.download.downloadplugindata.message.zipping")).queue();
        Path tempFile;
        try {
            tempFile = TempFile.createPath("download", "zip");
            try (var zip = new ZipFile(tempFile.toFile())) {
                zip.addFolder(pluginFile.toFile());
            }
            tempFile.toFile().deleteOnExit();
        } catch (ZipException e) {
            log.error("Failed to zip date", e);
            event.getHook().editOriginal(context.localize("command.server.download.downloadplugindata.message.fail.zip")).queue();
            return;
        } catch (IOException e) {
            log.error("Failed to create zip file", e);
            event.getHook().editOriginal(context.localize("command.server.download.downloadplugindata.message.fail.tempfile")).queue();
            return;
        }
        event.getHook().editOriginal(context.localize("command.server.download.downloadplugindata.message.success"))
             .setFiles(FileUpload.fromData(tempFile, pluginFile.toFile().getName() + ".zip"))
             .queue(RestAction.getDefaultSuccess(), err -> {
                 if (err instanceof ErrorResponseException response) {
                     if (response.getErrorResponse() == ErrorResponse.FILE_UPLOAD_MAX_SIZE_EXCEEDED) {
                         event.getHook().editOriginal(context.localize("command.server.download.downloadplugindata.message.fail.filetolarge")).queue();
                     }
                 }
             });
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
