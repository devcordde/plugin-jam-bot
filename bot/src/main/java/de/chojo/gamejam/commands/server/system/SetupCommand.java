/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.system;

import com.google.inject.Inject;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Bundle("locale")
@Interaction
public class SetupCommand {
    private static final Logger log = getLogger(SetupCommand.class);
    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public SetupCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "server system setup")
    public void onCommand(CommandEvent event) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();

        if (serverService.dockerService().exists(team.id())) {
            event.reply("command.server.system.setup.message.alreadysetup");
            return;
        }

        serverService.dockerService().provisionServer(team.id());
        event.reply("command.server.system.setup.message.success");
    }
}
