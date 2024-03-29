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

public class Restart implements SlashHandler {
    private final Server server;

    public Restart(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if(optServer.isEmpty())return;
        var teamServer = optServer.get();
        if (teamServer.exists()) {
            teamServer.stop(true)
                      .thenRun(() -> event.getHook().editOriginal(context.localize("command.server.process.restart.message.restarted")).queue());
            event.reply(context.localize("command.server.process.restart.message.restarting")).queue();
        }
    }
}
