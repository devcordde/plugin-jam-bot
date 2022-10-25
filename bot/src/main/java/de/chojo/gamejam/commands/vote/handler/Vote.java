/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.jams.jam.user.JamUser;
import de.chojo.gamejam.data.wrapper.votes.VoteEntry;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collections;
import java.util.stream.IntStream;

public class Vote implements SlashHandler {
    private final Guilds guilds;

    public Vote(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
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

        var teams = jam.teams();
        var teamList = teams.teams();
        var voteTeam = teamList.stream()
                             .filter(t -> t.meta().name().equalsIgnoreCase(event.getOption("team").getAsString()))
                             .findFirst();

        if (voteTeam.isEmpty()) {
            event.getHook().editOriginal(context.localize("error.unkownteam")).queue();
            return;
        }

        var team = teams.byMember(event.getMember());

        if (team.isPresent() && team.get().meta().name().equalsIgnoreCase(event.getOption("team").getAsString())) {
            event.getHook().editOriginal(context.localize("command.votes.vote.message.ownteam")).queue();
            return;
        }

        JamUser user = jam.user(event.getMember());

        var pointsGiven = jam.user(event.getMember()).votes().stream()
                             .mapToInt(VoteEntry::points)
                             .sum();

        //TODO: Max points and max points per team are currently hardcoded. should be configurable in the future.
        var points = Math.min(5, Math.max(0, event.getOption("points").getAsInt()));

        if (pointsGiven + points > teamList.size()) {
            event.getHook().editOriginal(context.localize("command.votes.vote.message.maxpointsreached",
                         Replacement.create("REMAINING", teamList.size() - pointsGiven)
                                 .addFormatting(Format.BOLD)))
                 .queue();
            return;
        }

        voteTeam.get().vote(event.getMember(), points);

        event.getHook().editOriginal(context.localize("command.votes.vote.message.done",
                     Replacement.create("REMAINING", teamList.size() - user.votes()
                                                                           .stream().mapToInt(VoteEntry::points)
                                                                           .sum()).addFormatting(Format.BOLD),
                     Replacement.create("POINTS", points).addFormatting(Format.BOLD),
                     Replacement.create("TEAM", voteTeam.get().meta().name()).addFormatting(Format.BOLD)))
             .queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var jam = guilds.guild(event).jams().nextOrCurrent();
        var option = event.getFocusedOption();
        if ("team".equals(option.getName())) {
            if (jam.isEmpty()) {
                event.replyChoices(Collections.emptyList()).queue();
                return;
            }
            var teams = jam.get().teams().teams().stream()
                           .filter(team -> team.matchName(option.getValue()))
                           .map(team -> team.meta().name())
                           .map(team -> new Command.Choice(team, team))
                           .toList();
            event.replyChoices(teams).queue();
        }
        if ("points".equals(option.getName())) {
            event.replyChoices(IntStream.range(0, 6).mapToObj(num -> new Command.Choice(String.valueOf(num), num))
                                        .toList()).queue();
        }
    }
}
