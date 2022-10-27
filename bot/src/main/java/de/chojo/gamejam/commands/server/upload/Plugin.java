/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.upload;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.commands.server.util.ProgressDownloader;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.slf4j.LoggerFactory.getLogger;

public class Plugin implements SlashHandler {
    private static final Logger log = getLogger(Plugin.class);
    private final Server server;

    public Plugin(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;
        var teamServer = optServer.get();

        var downloadUrl = event.getOption("file").getAsAttachment().getProxy().getUrl();

        var download = ProgressDownloader.download(event, context, downloadUrl);

        if (download.isEmpty()) return;

        var pluginFile = teamServer.plugins().resolve("plugin.jar");
        try {
            Files.copy(download.get(), pluginFile, StandardCopyOption.REPLACE_EXISTING);
            event.getHook().editOriginal("Added or replaced plugin.").queue();
        } catch (IOException e) {
            event.getHook().editOriginal("Failed to add plugin.").queue();
        }
    }
}
