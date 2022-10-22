/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.unregister.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class Handler implements SlashHandler {
    private final JamData jamData;
    private final TeamData teamData;

    public Handler(JamData jamData, TeamData teamData) {
        this.jamData = jamData;
        this.teamData = teamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Jam> optJam = jamData.getNextOrCurrentJam(event.getGuild());
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.noUpcomingJam"))
                 .setEphemeral(true)
                 .queue();
            return;
        }

        var jam = optJam.get();

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply(context.localize("command.unregister.notRegistered")).setEphemeral(true).queue();
            return;
        }

        teamData.getTeamByMember(jam, event.getMember())
                .ifPresentOrElse(
                        team -> event.reply(context.localize("command.unregister.inTeam")).queue(),
                        () -> {
                            var settings = jamData.getJamSettings(event.getGuild());
                            var role = event.getGuild().getRoleById(settings.jamRole());
                            if (role != null) {
                                event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                            }
                            event.reply(context.localize("command.unregister.unregistered"))
                                 .setEphemeral(true)
                                 .queue();

                        });
    }
}
