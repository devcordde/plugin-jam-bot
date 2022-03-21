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

public record Leave(TeamData teamData) implements SubCommand<Jam> {
    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        var team = teamData.getTeamByMember(jam, event.getMember()).join();
        if (team.isEmpty()) {
            event.reply("You are not part of a team").setEphemeral(true).queue();
            return;
        }
        event.reply("You left the team").setEphemeral(true).queue();
        teamData.leaveTeam(team.get(), event.getMember());
        event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById(team.get().roleId())).queue();
        event.getGuild().getTextChannelById(team.get().textChannelId()).sendMessage(event.getUser().getName() + " left the team.").queue();
    }
}
