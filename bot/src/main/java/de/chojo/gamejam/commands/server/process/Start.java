/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.process;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Start implements SlashHandler {
    private final Server server;

    public Start(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if(optServer.isEmpty())return;
        var teamServer = optServer.get();
        if (teamServer.exists()) {
            if (teamServer.start()) {
                event.reply("Server started").queue();
            } else {
                event.reply("Could not start server. It is already running or not set up.").queue();
            }
        }
    }
}
