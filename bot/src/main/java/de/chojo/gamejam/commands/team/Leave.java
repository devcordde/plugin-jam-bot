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

import java.util.Objects;

public final class Leave implements SubCommand<Jam> {
    private final TeamData teamData;

    public Leave(TeamData teamData) {
        this.teamData = teamData;
    }

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

    public TeamData teamData() {
        return teamData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Leave) obj;
        return Objects.equals(this.teamData, that.teamData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamData);
    }

    @Override
    public String toString() {
        return "Leave[" +
                "teamData=" + teamData + ']';
    }

}
