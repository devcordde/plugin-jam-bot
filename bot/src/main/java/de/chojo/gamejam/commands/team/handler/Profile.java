/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collections;

public final class Profile implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Profile(TeamData teamData, JamData jamData) {
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

        if (event.getOption("user") != null) {
            teamData.getTeamByMember(jam, event.getOption("user").getAsMember())
                    .ifPresentOrElse(team -> sendProfile(event, team, context),
                            () -> event.reply(context.localize("command.team.profile.message.nouserteam")).setEphemeral(true)
                                       .queue());
            return;
        }
        if (event.getOption("team") != null) {
            teamData.getTeamByName(jam, event.getOption("team").getAsString())
                    .ifPresentOrElse(team -> sendProfile(event, team, context),
                            () -> event.reply(context.localize("error.unkownteam")).setEphemeral(true).queue());
            return;
        }
        teamData.getTeamByMember(jam, event.getMember())
                .ifPresentOrElse(team -> sendProfile(event, team, context),
                        () -> event.reply(context.localize("error.noteam")).setEphemeral(true).queue());
    }

    private void sendProfile(SlashCommandInteractionEvent event, JamTeam team, EventContext context) {
        event.replyEmbeds(team.profileEmbed(teamData, context.guildLocalizer())).setEphemeral(true).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("team".equals(event.getFocusedOption().getName())) {
            var jam = jamData.getNextOrCurrentJam(event.getGuild());
            if (jam.isEmpty()) {
                event.replyChoices(Collections.emptyList()).queue();
                return;
            }
            var teams = jam.get().teams().stream()
                           .filter(team -> team.matchName(event.getFocusedOption().getValue()))
                           .map(JamTeam::name)
                           .map(team -> new Command.Choice(team, team))
                           .toList();
            event.replyChoices(teams).queue();
        }
    }
}
