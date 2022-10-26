/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.server.system;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class Setup implements SlashHandler {
    private static final Logger log = getLogger(Setup.class);
    private final ServerService serverService;
    private final Guilds guilds;

    public Setup(Guilds guilds, ServerService serverService) {
        this.serverService = serverService;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = guilds.guild(event).jams().activeJam();

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }

        var jam = optJam.get();
        var optTeam = jam.teams().byMember(event.getUser());

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        var team = optTeam.get();

        var teamServer = serverService.get(team);
        boolean setup;
        try {
            setup = teamServer.setup();
        } catch (IOException e) {
            log.error("Could not setup server", e);
            event.reply("Something went wrong during server setup").queue();
            return;
        }

        if (setup) {
            event.reply("Server was setup successfully.").queue();
        } else {
            event.reply("Server was already created.").queue();
        }
    }
}
