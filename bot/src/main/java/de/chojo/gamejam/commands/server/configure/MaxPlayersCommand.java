/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.configure;

import com.google.inject.Inject;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Bundle("locale")
@Interaction
public class MaxPlayersCommand {
    private static final Logger log = getLogger(MaxPlayersCommand.class);

    private final CommandContextProvider userContextService;
    private final ServerService serverService;

    @Inject
    public MaxPlayersCommand(CommandContextProvider commandContextProvider, ServerService serverService) {
        this.userContextService = commandContextProvider;
        this.serverService = serverService;
    }

    @Command(value = "server configure maxplayers")
    public void onCommand(CommandEvent event, @Param("amount" ) int amount) {
        var team = userContextService.getUserContext(event.getMember()).team();

        if (!serverService.dockerService().isRunning(team.id())) {
            event.reply("error.servernotrunning");
            return;
        }

        var containerName = serverService.dockerService().containerName(team.id());

        serverService.serverHttpService().configureMaxPlayers(containerName, amount);
        event.reply("command.server.configure.maxplayers.message.success");
    }
}
