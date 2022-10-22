/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public class List implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public List(TeamData teamData, JamData jamData) {
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = jamData.getNextOrCurrentJam(event.getGuild());
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.noJamActive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        context.registerPage(new PrivateListPageBag<>(jam.teams(), event.getUser().getIdLong()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> currentElement().profileEmbed(teamData, context.guildLocalizer()));
            }
        }, true);
    }
}
