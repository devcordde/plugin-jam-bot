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

public class Promote implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Promote(TeamData teamData, JamData jamData) {
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

        var user = event.getOption("user").getAsMember();
        teamData.getTeamByMember(jam, user)
                .ifPresentOrElse(
                        team -> {
                            if (team.leader() != event.getUser().getIdLong()) {
                                event.reply(context.localize("error.noLeader")).setEphemeral(true).queue();
                                return;
                            }

                            if (user.getRoles().stream().noneMatch(role -> role.getIdLong() == team.roleId())) {
                                event.reply(context.localize("command.team.promote.notInTeam")).queue();
                                return;
                            }

                            team.leader(user.getIdLong());
                            teamData.updateTeam(team);

                            event.reply(context.localize("command.team.promote.done")).setEphemeral(true).queue();

                        },
                        () -> event.reply(context.localize("error.noTeam")).setEphemeral(true).queue());
    }
}
