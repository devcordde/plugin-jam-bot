/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.serveradmin.handler.stop;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopAll implements SlashHandler {
    private final ServerService serverService;
    private final Guilds guilds;

    public StopAll(ServerService serverService, Guilds guilds) {
        this.serverService = serverService;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var currentJam = guilds.guild(event).jams().getCurrentJam();
        if (currentJam.isEmpty()) {
            event.reply(context.localize("error.noactivejam")).queue();
            return;
        }
        var jam = currentJam.get();

        var count = jam.teams().teams().stream()
                       .map(serverService::get)
                       .map(server -> {
                           var running = server.running();
                           server.stop(false);
                           return running;
                       })
                       .filter(v -> v)
                       .count();
        event.reply("Stopped " + count + " servers.").queue();
    }
}
