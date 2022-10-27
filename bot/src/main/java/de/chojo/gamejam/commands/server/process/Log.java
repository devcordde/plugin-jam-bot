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

import java.io.IOException;
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
        var logFile = teamServer.logFile();
        String content;
        try {
            content = Files.readString(logFile);
        } catch (IOException e) {
            content = "";
        }
        content = content.substring(Math.max(content.length() - 1950, 0));
        event.reply("```log%n%s%n```".formatted(content))
             .addFiles(FileUpload.fromData(logFile, "latest.log"))
             .queue();
    }
}
