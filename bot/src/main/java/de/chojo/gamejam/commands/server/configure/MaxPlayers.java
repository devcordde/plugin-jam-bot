/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.configure;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.server.TeamServer;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.slf4j.LoggerFactory.getLogger;

public class MaxPlayers implements SlashHandler {
    private static final Logger log = getLogger(MaxPlayers.class);
    private final Server server;

    public MaxPlayers(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;

        var teamServer = optServer.get();
        var request = teamServer.requestBuilder("v1/config/maxplayers")
                                .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(event.getOption("amount").getAsInt())))
                                .build();

        if (!teamServer.running()) {
            event.reply(context.localize("error.servernotrunning")).queue();
            return;
        }

        teamServer.http().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                  .whenComplete(Futures.whenComplete(res -> event.reply("command.server.configure.maxplayers.message.success").queue(),
                         err -> log.error("Failed to send request.", err)));;
    }
}
