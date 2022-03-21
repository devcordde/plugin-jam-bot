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

public final class Disband implements SubCommand<Jam> {
    private final TeamData teamData;

    public Disband(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        if (event.getOption("confirm").getAsBoolean()) {
            event.reply("Please confirm.").setEphemeral(true).queue();
            return;
        }

        var jamTeam = teamData.getTeamByMember(jam, event.getMember()).join();
        if (jamTeam.isEmpty()) {
            event.reply("You are not part of a team").setEphemeral(true).queue();
            return;
        }
        var members = teamData.getMember(jamTeam.get()).join();
        for (var teamMember : members) {
            event.getJDA().getShardManager().retrieveUserById(teamMember.userId())
                    .flatMap(u -> event.getGuild().retrieveMember(u))
                    .queue(member -> {
                        event.getGuild().removeRoleFromMember(member, event.getGuild().getRoleById(jamTeam.get().roleId())).queue();
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Your team was disbanded")).queue();
                    });
        }
        event.reply("Team disbanded").setEphemeral(true).queue();
        jamTeam.get().delete(event.getGuild());
        teamData.disbandTeam(jamTeam.get());
    }

    public TeamData teamData() {
        return teamData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Disband) obj;
        return Objects.equals(this.teamData, that.teamData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamData);
    }

    @Override
    public String toString() {
        return "Disband[" +
                "teamData=" + teamData + ']';
    }

}
