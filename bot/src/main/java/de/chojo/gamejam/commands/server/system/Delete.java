/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.system;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class Delete implements SlashHandler {
    private static final Logger log = getLogger(Delete.class);
    private final Server server;

    public Delete(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if(optServer.isEmpty())return;
        var teamServer = optServer.get();
        boolean deleted;
        try {
            deleted = teamServer.purge();
        } catch (IOException e) {
            log.error("Could not purge server", e);
            event.reply("Something went wrong during server deletion").queue();
            return;
        }

        if (deleted) {
            event.reply("Server was deleted successfully.").queue();
        } else {
            event.reply("Server is not set up.").queue();
        }
    }
}
