/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Leave implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Leave(TeamData teamData, JamData jamData) {
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = jamData.getNextOrCurrentJam(event.getGuild());
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.noJamActive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("error.votingActive")).setEphemeral(true).queue();
            return;
        }

        teamData.getTeamByMember(jam, event.getMember())
                .ifPresentOrElse(team -> {
                    if (team.leader() == event.getMember().getIdLong()) {
                        event.reply(context.localize("command.team.leave.leaderLeave")).setEphemeral(true).queue();
                        return;
                    }
                    team.leave(event, context, teamData);
                }, () -> event.reply(context.localize("error.noTeam")).setEphemeral(true).queue());
    }
}
