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

        var team = jamTeam.get();

        if (team.leader() == event.getMember().getIdLong()) {
            event.reply("The leader cant leave the team.").setEphemeral(true).queue();
            return;
        }

        var members = teamData.getMember(team).join();
        for (var teamMember : members) {
            event.getJDA().getShardManager().retrieveUserById(teamMember.userId())
                    .flatMap(u -> event.getGuild().retrieveMember(u))
                    .queue(member -> {
                        event.getGuild().removeRoleFromMember(member, event.getGuild().getRoleById(team.roleId())).queue();
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage("Your team was disbanded")).queue();
                    });
        }

        team.delete(event.getGuild());
        teamData.disbandTeam(team).thenRun(() -> event.reply("Team disbanded").setEphemeral(true).queue());
    }
}
