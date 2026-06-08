/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.process;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Log implements SlashHandler {
    private final Server server;

    public Log(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if(optServer.isEmpty())return;
        var teamServer = optServer.get();
        var logs = teamServer.logs(0);
        var content = logs.substring(Math.max(logs.length() - 1950, 0));
        try(InputStream inputStream = new ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8))) {
            event.reply("```log%n%s%n```".formatted(content))
                    .addFiles(FileUpload.fromData(inputStream, "latest.log"))
                    .queue();
        } catch (IOException _) {
        }
    }
}
