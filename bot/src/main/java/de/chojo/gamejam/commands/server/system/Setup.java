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

public class Setup implements SlashHandler {
    private static final Logger log = getLogger(Setup.class);
    private final Server server;

    public Setup(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if(optServer.isEmpty())return;
        var teamServer = optServer.get();
        boolean setup;
        try {
            setup = teamServer.setup();
        } catch (IOException e) {
            log.error("Could not setup server", e);
            event.reply(context.localize("command.server.system.setup.message.error")).queue();
            return;
        }

        if (setup) {
            event.reply(context.localize("command.server.system.setup.message.success")).queue();
        } else {
            event.reply(context.localize("command.server.system.setup.message.alreadysetup")).queue();
        }
    }
}
