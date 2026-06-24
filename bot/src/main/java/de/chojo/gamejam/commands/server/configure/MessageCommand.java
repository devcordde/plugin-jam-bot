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
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Bundle("locale")
@Interaction
public class MessageCommand {
    private static final Logger log = getLogger(MessageCommand.class);

    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public MessageCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Command
    public void onCommand(CommandEvent event, @Param("message") String message) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();

        if (!serverService.dockerService().isRunning(team.id())) {
            event.reply("error.servernotrunning");
            return;
        }

        event.replyModal("onModal",
                Label.of("command.server.configure.message.message.modal.input.message.label", TextInput.create("message-input", TextInputStyle.SHORT)
                        .setPlaceholder("command.server.configure.message.message.modal.input.message.placeholder")
                        .build())
        );

        serverService.serverHttpService().configureMessage(serverService.dockerService().containerName(team.id()), message);
        event.reply("command.server.configure.message.message.success");
    }

    @Modal("command.server.configure.message.message.modal.title")
    public void onModal(ModalEvent event) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();
        var containerName = serverService.dockerService().containerName(team.id());
        var content = event.value("message-input").getAsString();
        serverService.serverHttpService().configureMessage(containerName, content);
        event.reply("command.server.configure.message.message.success");
    }
}
