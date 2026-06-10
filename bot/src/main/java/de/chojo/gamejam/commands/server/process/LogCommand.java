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
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Bundle("locale")
@Interaction
public class LogCommand {
    private final ServerService serverService;
    private final CommandContextProvider commandContextProvider;

    @Inject
    public LogCommand(ServerService serverService, CommandContextProvider commandContextProvider) {
        this.serverService = serverService;
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "server process log")
    public void onCommand(CommandEvent event) {
        var team = commandContextProvider.getUserContext(event.getMember()).team();
        var dockerService = serverService.dockerService();

        var logs = dockerService.logs(team.id());

        var content = logs.substring(Math.max(logs.length() - 1950, 0));
        try(InputStream inputStream = new ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8))) {
            event.reply("```log%n%s%n```".formatted(content))
                    .replyFiles(FileUpload.fromData(inputStream, "latest.log"))
                    .queue();
        } catch (IOException _) {
        }
    }
}
