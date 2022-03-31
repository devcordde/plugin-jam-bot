/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public class List implements SubCommand<Jam> {
    private final TeamData teamData;

    public List(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        context.registerPage(new PrivateListPageBag<>(jam.teams(), event.getUser().getIdLong()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> currentElement().profileEmbed(teamData, context.localizer()));
            }
        }, true);
    }
}
