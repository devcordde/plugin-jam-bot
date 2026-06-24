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
public class DeleteCommand {
    private static final Logger log = getLogger(DeleteCommand.class);

    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public DeleteCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "server system delete")
    public void onCommand(CommandEvent event) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();

        if (!serverService.dockerService().exists(team.id())) {
            event.reply("command.server.system.delete.message.notsetup");
            return;
        }

        serverService.dockerService().destroyServer(team.id());
        event.reply("command.server.system.delete.message.success");
    }
}
