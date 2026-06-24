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
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public class ConsoleCommand  {
    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public ConsoleCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

     @Command(value = "server process console")
     public void onCommand(CommandEvent event, @Param("command") String command) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();

        if (!serverService.dockerService().isRunning(team.id())) {
            event.reply("error.servernotrunning");
            return;
        }

         if (command.startsWith("stop") || command.startsWith("restart")) {
             event.reply("command.server.process.console.message.notexecutable");
             return;
         }

        serverService.dockerService().sendCommand(team.id(), command);
        event.reply("command.server.process.console.message.executed");
     }
}
