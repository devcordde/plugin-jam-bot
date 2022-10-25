/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.system;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Setup implements SlashHandler {
    private final ServerService serverService;

    public Setup(ServerService serverService, Guilds guilds) {
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
    }
}
