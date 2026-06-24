/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.process;

import com.google.inject.Inject;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public class RestartCommand {

    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public RestartCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "server process restart")
    public void onCommand(CommandEvent event) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();
        var dockerService = serverService.dockerService();

        if (!dockerService.isRunning(team.id())) {
            event.reply("error.servernotrunning");
            return;
        }

        var teamServer = serverService.get(team);

        serverService.restartServer(teamServer);
        event.reply("command.server.process.restart.message.restarting");
    }
}
