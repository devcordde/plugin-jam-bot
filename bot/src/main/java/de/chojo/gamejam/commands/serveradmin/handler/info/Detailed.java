/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.serveradmin.handler.info;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Detailed implements SlashHandler {
    private final ServerService serverService;
    private final Guilds guilds;

    public Detailed(ServerService serverService, Guilds guilds) {
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

        var teamArg = event.getOption("team");

        if (teamArg != null) {
            var team = jam.teams().byName(teamArg.getAsString());
            if (team.isEmpty()) {
                event.reply(context.localize("error.unkownteam")).queue();
                return;
            }
            var teamServer = serverService.get(team.get());
            event.replyEmbeds(teamServer.detailStatus(context).join()).queue();
            return;
        }

        var servers = jam.teams().teams().stream()
                         .map(serverService::get)
                         .toList();

        context.registerPage(new ListPageBag<>(servers) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return currentElement().detailStatus(context);
            }
        });
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
