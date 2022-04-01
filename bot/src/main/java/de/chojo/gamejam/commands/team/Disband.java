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
        if(jam.state().isVoting()){
            event.reply(context.localize("error.votingActive")).setEphemeral(true).queue();
            return;
        }

        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply(context.localize("error.noConfirm")).setEphemeral(true).queue();
            return;
        }

        var jamTeam = teamData.getTeamByMember(jam, event.getMember()).join();
        if (jamTeam.isEmpty()) {
            event.reply(context.localize("error.noTeam")).setEphemeral(true).queue();
            return;
        }

        var team = jamTeam.get();


        var members = teamData.getMember(team).join();
        for (var teamMember : members) {
            event.getJDA().getShardManager().retrieveUserById(teamMember.userId())
                    .flatMap(u -> event.getGuild().retrieveMember(u))
                    .queue(member -> {
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(context.localize("command.team.disband.disbanded"))).queue();
                    });
        }

        team.delete(event.getGuild());
        teamData.disbandTeam(team).thenRun(() -> event.reply(context.localize("command.team.disband.disbanded")).setEphemeral(true).queue());
    }
}
