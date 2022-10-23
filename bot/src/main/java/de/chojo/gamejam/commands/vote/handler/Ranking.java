/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public class Ranking implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Ranking(TeamData teamData, JamData jamData) {
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = jamData.getNextOrCurrentJam(event.getGuild());
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.message.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("command.votes.ranking.message.voteactive")).setEphemeral(true).queue();
            return;
        }

        var ranking = teamData.votesByJam(jam);

        var pageBag = new ListPageBag<>(ranking) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                var teamVote = currentElement();
                var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                        .setTitle(teamVote.rank() + " | " + teamVote.jamTeam().name())
                        .addField("command.votes.ranking.embed.votes", String.valueOf(teamVote.votes()), true)
                        .build();
                return CompletableFuture.completedFuture(embed);
            }
        };
        context.registerPage(pageBag, true);
    }
}
