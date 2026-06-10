/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote;

import com.google.inject.Inject;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
        var guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }

        var jam = optJam.get();

        event.deferReply(true);
        if (!jam.state().isVoting()) {
            event.reply("command.votes.vote.message.notactive");
            return;
        }

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply("error.notregistered");
            return;
        }

        var teams = jam.teams();
        var teamCount = teams.teams().size();
        var optVoteTeam = teams.byName(team);

        if (optVoteTeam.isEmpty()) {
            event.reply("error.unkownteam");
            return;
        }

        var voteTeam = optVoteTeam.get();

        if (voteTeam.member(event.getMember()).isPresent()) {
            event.reply("command.votes.vote.message.ownteam");
            return;
        }

        var user = jam.user(event.getMember());

        var pointsGiven = user.votesGiven();

        //TODO: Max points and max points per team are currently hardcoded. should be configurable in the future.
        var finalPoints = Math.clamp(points, 0, 5);

        var votes = voteTeam.votes(event.getMember());

        if (votes < finalPoints && pointsGiven + finalPoints > teamCount) {
            event.reply("command.votes.vote.message.maxpointsreached", Entry.entry("REMAINING", bold(String.valueOf(teamCount - pointsGiven))));
            return;
        }

        voteTeam.vote(event.getMember(), finalPoints);

        event.reply("command.votes.vote.message.done",
                        Entry.entry("REMAINING", bold(String.valueOf(teamCount - user.votesGiven()))),
                        Entry.entry("POINTS", bold(String.valueOf(finalPoints))),
                        Entry.entry("TEAM", bold(voteTeam.meta().name()))
        );
    }

    @AutoComplete(value = "vote", options = "team")
    public void onAutoCompleteTeam(AutoCompleteEvent event) {
        var jam = commandContextProvider.guilds().guild(event).jams().nextOrCurrent();

        if (jam.isEmpty()) {
            event.replyChoices(Collections.emptyList());
            return;
        }
        var teams = jam.get().teams().teams().stream()
                .filter(team -> team.matchName(event.getValue()))
                .map(team -> team.meta().name())
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
