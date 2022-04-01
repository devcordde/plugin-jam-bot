/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.localization.util.Replacement;
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
                    if (team.leader() == event.getMember().getIdLong()) {
                        event.reply(context.localize("command.team.leave.leaderLeave")).setEphemeral(true).queue();
                        return;
                    }
                    team.leave(event, context, teamData);
                }, () -> event.reply(context.localize("error.noTeam")).setEphemeral(true).queue());
    }
}
