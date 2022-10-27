/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.upload;

import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.commands.server.util.ProgressDownloader;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class World implements SlashHandler {
    private static final Logger log = getLogger(World.class);
    private final Server server;

    public World(Server server) {
        this.server = server;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optServer = server.getServer(event, context);
        if (optServer.isEmpty()) return;
        var teamServer = optServer.get();

        String downloadUrl = null;

        var urlOption = event.getOption("url");
        if (urlOption != null) {
            downloadUrl = urlOption.getAsString();
        }

        var file = event.getOption("file");
        if (file != null) {
            downloadUrl = file.getAsAttachment().getProxy().getUrl();
        }

        if (downloadUrl == null) {
            event.reply("No file or url provided").queue();
            return;
        }

        var download = ProgressDownloader.download(event, context, downloadUrl);

        if (download.isEmpty()) return;

        event.getHook().editOriginal("Download done. Replacing.").queue();
        if (teamServer.replaceWorld(download.get())) {
            event.getHook().editOriginal("Replaced world").queue();
        } else {
            event.getHook().editOriginal("Failed to replace world").queue();
        }
    }
}
