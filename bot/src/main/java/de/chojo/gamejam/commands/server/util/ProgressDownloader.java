/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.util;

import de.chojo.gamejam.util.TempFile;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public final class ProgressDownloader {
    private static final Logger log = getLogger(ProgressDownloader.class);

    private ProgressDownloader() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static Optional<Path> download(SlashCommandInteractionEvent event, EventContext context, String downloadUrl) {
        event.reply(context.localize("command.server.util.progressdownloader.message.attempting")).queue();

        Path path;
        try {
            path = TempFile.createFile("gamejam", ".file");
        } catch (IOException e) {
            log.error("Failed to create temp file", e);
            event.getHook().editOriginal(context.localize("command.server.util.progressdownloader.message.fail.tempfile")).queue();
            return Optional.empty();
        }

        var request = HttpRequest.newBuilder(URI.create(downloadUrl)).GET().build();

        try {
            event.getHook().editOriginal(context.localize("command.server.util.progressdownloader.message.downloading")).queue();
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(path));
        } catch (IOException e) {
            log.error("Failed to write response", e);
            event.getHook().editOriginal(context.localize("command.server.util.progressdownloader.message.fail.download")).queue();
            return Optional.empty();
        } catch (InterruptedException e) {
            log.error("Failed to retrieve response", e);
            event.getHook().editOriginal(context.localize("command.server.util.progressdownloader.message.fail.download")).queue();
            return Optional.empty();
        }

        event.getHook().editOriginal(context.localize("command.server.util.progressdownloader.message.done")).queue();
        return Optional.of(path);
    }
}
