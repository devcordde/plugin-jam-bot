/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.vote;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collections;
import java.util.stream.IntStream;

import static net.dv8tion.jda.api.utils.MarkdownUtil.bold;

@Bundle("locale")
@Interaction
public class VoteCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public VoteCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @io.github.kaktushose.jdac.annotations.interactions.Command(value = "vote")
    public void onCommand(CommandEvent event, @Param("team") String team, @Param("points") int points) {
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }

        var jam = optJam.get();

        event.deferReply(true);
        if (!jam.state().voting()) {
            event.reply("command-votes-vote-message-notactive");
            return;
        }

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply("error-notregistered");
            return;
        }

        var teams = commandContextProvider.teamService().getTeamsByJamId(jam.id());
        var teamCount = teams.size();
        var optVoteTeam = commandContextProvider.teamService().getTeamByName(jam.id(), team);

        if (optVoteTeam.isEmpty()) {
            event.reply("error-unkownteam");
            return;
        }

        var voteTeam = optVoteTeam.get();

        if (voteTeam.isMember(event.getUser())) {
            event.reply("command-votes-vote-message-ownteam");
            return;
        }

        var pointsGiven = commandContextProvider.voteService().getGivenPointsByUser(event.getUser());

        //TODO: Max points and max points per team are currently hardcoded. should be configurable in the future.
        var finalPoints = Math.clamp(points, 0, 5);

        var votesForTeam = commandContextProvider.voteService().getPointsByTeam(voteTeam);

        if (votesForTeam < finalPoints && pointsGiven + finalPoints > teamCount) {
            event.reply("command-votes-vote-message-maxpointsreached", Entry.entry("REMAINING", bold(String.valueOf(teamCount - pointsGiven))));
            return;
        }

        commandContextProvider.voteService().voteForTeam(event.getUser(), voteTeam, finalPoints);
        var pointsGivenAfter = commandContextProvider.voteService().getGivenPointsByUser(event.getUser());

        event.reply("command-votes-vote-message-done",
                        Entry.entry("REMAINING", bold(String.valueOf(teamCount - pointsGivenAfter))),
                        Entry.entry("POINTS", bold(String.valueOf(finalPoints))),
                        Entry.entry("TEAM", bold(voteTeam.meta().getTeamName()))
        );
    }

    @AutoComplete(value = "vote", options = "team")
    public void onAutoCompleteTeam(AutoCompleteEvent event) {
        var jam = commandContextProvider.pluginJamService().getActiveJam(event.getGuild().getIdLong());

        if (jam.isEmpty()) {
            event.replyChoices(Collections.emptyList());
            return;
        }

        var teams = commandContextProvider.teamService().getTeamsByJamId(jam.get().id()).stream()
                .filter(team -> team.meta().getTeamName().contains(event.getValue()))
                .map(team -> team.meta().getTeamName())
                .map(team -> new Command.Choice(team, team))
                .toList();
        event.replyChoices(teams);
    }

    @AutoComplete(value = "vote", options = "points")
    public void onAutoCompletePoints(AutoCompleteEvent event) {
        event.replyChoices(IntStream.range(0, 6).mapToObj(num -> new Command.Choice(String.valueOf(num), num))
                .toList());
    }
}
