/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.gamejam.data.wrapper.votes.VoteEntry;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;

public class Vote implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Vote(TeamData teamData, JamData jamData) {
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

        event.deferReply(true).queue();
        if (!jam.state().isVoting()) {
            event.getHook().editOriginal(context.localize("command.votes.vote.message.notactive")).queue();
            return;
        }

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.getHook().editOriginal(context.localize("error.notregistered")).queue();
            return;
        }

        var voteTeam = jam.teams().stream()
                          .filter(t -> t.name().equalsIgnoreCase(event.getOption("team").getAsString()))
                          .findFirst();

        if (voteTeam.isEmpty()) {
            event.getHook().editOriginal(context.localize("error.unkownteam")).queue();
            return;
        }

        var team = teamData.getTeamByMember(jam, event.getMember());
//        if (team.isEmpty()) {
//            event.getHook().editOriginal(context.localize("command.votes.vote.noTeam")).queue();
//            return;
//        }

        if (team.isPresent() && team.get().name().equalsIgnoreCase(event.getOption("team").getAsString())) {
            event.getHook().editOriginal(context.localize("command.votes.vote.message.ownteam")).queue();
            return;
        }

        var pointsGiven = teamData.votesByUser(event.getMember(), jam).stream().mapToInt(VoteEntry::points)
                                  .sum();

        //TODO: Max points and max points per team are currently hardcoded. should be configurable in the future.
        var points = Math.min(5, Math.max(0, event.getOption("points").getAsInt()));

        if (pointsGiven + points > jam.teams().size()) {
            event.getHook().editOriginal(context.localize("command.votes.vote.message.maxpointsreached",
                         Replacement.create("REMAINING", jam.teams().size() - pointsGiven).addFormatting(Format.BOLD)))
                 .queue();
            return;
        }

        teamData.vote(event.getMember(), voteTeam.get(), points);

        event.getHook().editOriginal(context.localize("command.votes.vote.message.done",
                     Replacement.create("REMAINING", jam.teams()
                                                        .size() - teamData.votesByUser(event.getMember(), jam)
                                                                          .stream().mapToInt(VoteEntry::points)
                                                                          .sum()).addFormatting(Format.BOLD),
                     Replacement.create("POINTS", points).addFormatting(Format.BOLD),
                     Replacement.create("TEAM", voteTeam.get().name()).addFormatting(Format.BOLD)))
             .queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("team".equals(option.getName())) {
            Optional<Jam> jam = jamData.getNextOrCurrentJam(event.getGuild());
            if (jam.isEmpty()) {
                event.replyChoices(Collections.emptyList()).queue();
                return;
            }
            var teams = jam.get().teams().stream()
                           .filter(team -> team.matchName(option.getValue()))
                           .map(JamTeam::name)
                           .map(team -> new Command.Choice(team, team))
                           .toList();
            event.replyChoices(teams).queue();
        }
        if ("points".equals(option.getName())) {
            event.replyChoices(IntStream.range(0, 6).mapToObj(num -> new Command.Choice(String.valueOf(num), num)).toList()).queue();
        }
    }
}
