/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.serveradmin.handler.start;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.TeamServer;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class StartTeam implements SlashHandler {
    private final ServerService serverService;
    private final Guilds guilds;

    public StartTeam(ServerService serverService, Guilds guilds) {
        this.serverService = serverService;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var currentJam = guilds.guild(event).jams().getCurrentJam();
        if (currentJam.isEmpty()) {
            event.reply(context.localize("error.noactivejam")).queue();
            return;
        }
        var jam = currentJam.get();

        var optTeam = jam.teams().byName(event.getOption("team").getAsString());

        if (optTeam.isEmpty()) {
            event.reply(context.localize("error.unkownteam")).queue();
            return;
        }

        var started = optTeam.map(serverService::get).map(TeamServer::start).orElse(false);
        if (started) {
            event.reply("Server of team " + optTeam.get() + " started.").queue();
        } else {
            event.reply("Could not start server of team" + optTeam.get() + " servers.").queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event);
        var option = event.getFocusedOption();
        if ("team".equals(option.getName())) {
            var choices = guild.jams().nextOrCurrent()
                               .map(jam -> jam.teams().completeTeam(option.getValue()))
                               .orElse(Collections.emptyList());
            event.replyChoices(choices).queue();
        }
    }
}
