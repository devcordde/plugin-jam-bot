/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.configure;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.slf4j.Logger;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.slf4j.LoggerFactory.getLogger;

public class Message implements SlashHandler {
    private static final Logger log = getLogger(Message.class);
    private final Server server;

    public Message(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;

        var teamServer = optServer.get();

        if (!teamServer.running()) {
            event.reply(context.localize("error.servernotrunning")).queue();
            return;
        }

        context.registerModal(ModalHandler.builder(context.localize("command.server.configure.message.message.modal.title"))
                .addInput(TextInputHandler.builder("message", context.localize("command.server.configure.message.message.modal.input.message.label"), TextInputStyle.PARAGRAPH)
                        .withPlaceholder(context.localize("command.server.configure.message.message.modal.input.message.placeholder"))
                        .build())
                .withHandler(modalEvent -> {
                    var content = modalEvent.getValues().get(0).getAsString();
                    var request = teamServer.requestBuilder("v1/config/message")
                                            .POST(HttpRequest.BodyPublishers.ofString(content))
                                            .build();
                    teamServer.http().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                              .whenComplete(Futures.whenComplete(res -> modalEvent.reply(context.localize("command.server.configure.message.message.success")).queue(),
                                      err -> log.error("Failed to send request.", err)));
                })
                .build());
    }
}
