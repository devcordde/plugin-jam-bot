/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.vote;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import de.chojo.pluginjam.database.entity.VotingRank;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Bundle("locale")
@Interaction
public class VotesRankingCommand {
    private final CommandContextProvider commandContextProvider;
    private final MessageResolver messageResolver;

    private List<VotingRank> pageEntries;
    private int currentPage;

    @Inject
    public VotesRankingCommand(CommandContextProvider commandContextProvider, MessageResolver messageResolver) {
        this.commandContextProvider = commandContextProvider;
        this.messageResolver = messageResolver;
    }

    @Command(value = "votes ranking")
    public void onCommand(CommandEvent event) {
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getActiveJam(guildId);

        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }

        var jam = optJam.get();

        if (jam.state().voting()) {
            event.with().ephemeral(true).reply("command-votes-ranking-message-voteactive");
            return;
        }

        pageEntries = commandContextProvider.voteService().getVotingRanks(jam.id());
        currentPage = 0;
        event.with().ephemeral(true).reply(renderPage(0, pageEntries, event.getUserLocale()));
    }

    private Container renderPage(int page, List<VotingRank> entries, DiscordLocale locale) {
        int pageSize = 10;

        boolean hasNext = page < (entries.size() - 1) / pageSize;
        boolean hasPrev = page > 0;

        int start = page * pageSize;
        int end = Math.min(start + pageSize, entries.size());

        var contents = new ArrayList<ContainerChildComponent>();

        var rankingContent = entries.subList(start, end).stream()
                .map(votingRank -> String.format("`%s` **%s** (*%d Votes*)",
                        getRankFormat(votingRank.rank()),
                        votingRank.team().meta().getTeamName(),
                        votingRank.points()))
                .collect(Collectors.joining("\n"));

        contents.add(TextDisplay.of("## " + messageResolver.resolve("command-votes-ranking-description", locale)));
        contents.add(Separator.create(true, Separator.Spacing.LARGE));
        contents.add(TextDisplay.of(rankingContent));
        contents.add(Separator.create(true, Separator.Spacing.LARGE));

        int maxPages = (int) Math.ceil(entries.size() / (double) pageSize);

        contents.add(
                ActionRow.of(
                        hasPrev ? Component.enabled("onPrev") : Component.disabled("onPrev"),
                        net.dv8tion.jda.api.components.buttons.Button.of(ButtonStyle.SECONDARY, UUID.randomUUID().toString(), "%d / %d".formatted(currentPage + 1, maxPages), null),
                        hasNext ? Component.enabled("onNext") : Component.disabled("onNext")
                )
        );

        return Container.of(contents);
    }

    @Button(value = "◀", style = ButtonStyle.SECONDARY)
    public void onPrev(ComponentEvent event) {
        currentPage--;
        event.with().keepComponents(false).ephemeral(true).reply(renderPage(currentPage, pageEntries, event.getUserLocale()));
    }

    @Button(value = "▶", style = ButtonStyle.SECONDARY)
    public void onNext(ComponentEvent event) {
        currentPage++;
        event.with().keepComponents(false).ephemeral(true).reply(renderPage(currentPage, pageEntries, event.getUserLocale()));
    }

    private String getRankFormat(int rank) {
        return switch (rank) {
            case 1 -> "🥇1";
            case 2 -> "🥈2";
            case 3 -> "🥉3";
            default -> " #%d".formatted(rank);
        };
    }

}
