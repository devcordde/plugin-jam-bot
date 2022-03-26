/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Leave implements SubCommand<Jam> {
    private final TeamData teamData;

    public Leave(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        teamData.getTeamByMember(jam, event.getMember()).join()
                .ifPresentOrElse(team -> {
                    event.reply("You left the team").setEphemeral(true).queue();
                    teamData.leaveTeam(team, event.getMember());
                    event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(team.roleId())).queue();
                    event.getGuild().getTextChannelById(team.textChannelId()).sendMessage(event.getUser().getName() + " left the team.").queue();
                }, () -> event.reply("You are not part of a team").setEphemeral(true).queue());
    }
}
