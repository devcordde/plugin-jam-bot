/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public class Ranking implements SubCommand<Jam> {
    private final TeamData teamData;

    public Ranking(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        if (jam.state().isVoting()) {
            event.reply(context.localize("command.votes.ranking.voteActive")).setEphemeral(true).queue();
            return;
        }

        var ranking = teamData.votesByJam(jam).join();

        var pageBag = new ListPageBag<>(ranking) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                var teamVote = currentElement();
                var embed = new LocalizedEmbedBuilder(context.localizer())
                        .setTitle(teamVote.rank() + " | " + teamVote.jamTeam().name())
                        .addField("command.votes.ranking.votes", String.valueOf(teamVote.votes()), true)
                        .build();
                return CompletableFuture.completedFuture(embed);
            }
        };
        context.registerPage(pageBag, true);
    }
}
