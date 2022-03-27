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

public class Promote implements SubCommand<Jam> {
    private final TeamData teamData;

    public Promote(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        var user = event.getOption("user").getAsMember();
        teamData.getTeamByMember(jam, user)
                .thenAccept(optTeam -> {
                    if (optTeam.isEmpty()) {
                        event.reply(context.localize("error.noTeam")).setEphemeral(true).queue();
                        return;
                    }

                    var team = optTeam.get();

                    if (team.leader() != event.getUser().getIdLong()) {
                        event.reply(context.localize("error.noLeader")).setEphemeral(true).queue();
                        return;
                    }

                    if (user.getRoles().stream().noneMatch(role -> role.getIdLong() == team.roleId())) {
                        event.reply(context.localize("command.team.promote.notInTeam")).queue();
                        return;
                    }

                    team.leader(user.getIdLong());
                    teamData.updateTeam(team).thenRun(() -> event.reply(context.localize("command.team.promote.done")).setEphemeral(true).queue());
                });

    }
}
