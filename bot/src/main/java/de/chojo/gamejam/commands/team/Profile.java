/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Profile implements SubCommand<Jam> {
    private final TeamData teamData;

    public Profile(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        if (event.getOption("user") != null) {
            teamData.getTeamByMember(jam, event.getOption("user").getAsMember()).join()
                    .ifPresentOrElse(team -> sendProfile(event, team, context),
                            () -> event.reply(context.localize("command.team.profile.noUserTeam")).setEphemeral(true).queue());
            return;
        }
        if (event.getOption("team") != null) {
            teamData.getTeamByName(jam, event.getOption("team").getAsString()).join()
                    .ifPresentOrElse(team -> sendProfile(event, team, context),
                            () -> event.reply(context.localize("error.unkownTeam")).setEphemeral(true).queue());
            return;
        }
        teamData.getTeamByMember(jam, event.getMember()).join()
                .ifPresentOrElse(team -> sendProfile(event, team, context),
                        () -> event.reply(context.localize("error.noTeam")).setEphemeral(true).queue());
    }

    private void sendProfile(SlashCommandInteractionEvent event, JamTeam team, SlashCommandContext context) {
        event.replyEmbeds(team.profileEmbed(teamData, context.localizer())).setEphemeral(true).queue();
    }
}
