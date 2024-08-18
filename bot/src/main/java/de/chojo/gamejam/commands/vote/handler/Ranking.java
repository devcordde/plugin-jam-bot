/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.concurrent.CompletableFuture;

public class Ranking implements SlashHandler {
    private final Guilds guilds;

    public Ranking(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }

        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("command.votes.ranking.message.voteactive")).setEphemeral(true).queue();
            return;
        }

        var ranking = jam.votes();

        var pageBag = new ListPageBag<>(ranking) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                var teamVote = currentElement();
                var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                        .setTitle(teamVote.rank() + " | " + teamVote.team().meta().name())
                        .addField("command.votes.ranking.embed.votes", String.valueOf(teamVote.votes()), true)
                        .build();
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(embed));
            }
        };
        context.registerPage(pageBag, true);
    }
}
