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
public class SpectatorOverflowCommand {
    private static final Logger log = getLogger(SpectatorOverflowCommand.class);
    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public SpectatorOverflowCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "server configure spectatoroverflow")
    public void onCommand(CommandEvent event, @Param("state") boolean state) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();

        if (!serverService.dockerService().isRunning(team.id())) {
            event.reply("error.servernotrunning");
            return;
        }

        serverService.serverHttpService().configureSpectatorOverflow(serverService.dockerService().containerName(team.id()), state);
        event.reply("command.server.configure.spectatoroverflow.message.success");
    }
}
