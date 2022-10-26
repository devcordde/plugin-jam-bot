/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.upload;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
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

import static org.slf4j.LoggerFactory.getLogger;

public class Plugin implements SlashHandler {
    private static final Logger log = getLogger(Plugin.class);
    private final Guilds guilds;
    private final ServerService serverService;

    public Plugin(Guilds guilds, ServerService serverService) {
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

        event.reply("Attempting to download plugin").queue();

        Path file;
        try {
            file = Files.createTempFile("plugin", String.valueOf(System.currentTimeMillis()));
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
        var pluginFile = teamServer.plugins().resolve("plugin.jar");
        try {
            Files.copy(file, pluginFile);
            event.getHook().editOriginal("Added or replaced plugin.").queue();
        } catch (IOException e) {
            event.getHook().editOriginal("Failed to add plugin.").queue();
        }
    }
}
