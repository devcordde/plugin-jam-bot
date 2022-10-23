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

public class Rename implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Rename(TeamData teamData, JamData jamData) {
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = jamData.getNextOrCurrentJam(event.getGuild());
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.message.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        teamData.getTeamByName(jam, event.getOption("name").getAsString())
                .ifPresentOrElse(
                        team -> event.reply(context.localize("command.team.create.message.nametaken")).setEphemeral(true)
                                     .queue(),
                        () -> {
                            var optCurrTeam = teamData.getTeamByMember(jam, event.getUser());

                            if (optCurrTeam.isEmpty()) {
                                event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
                                return;
                            }

                            var team = optCurrTeam.get();

                            if (team.leader() != event.getUser().getIdLong()) {
                                event.reply(context.localize("error.noleader")).setEphemeral(true).queue();
                                return;
                            }

                            team.rename(event.getGuild(), event.getOption("name").getAsString());
                            teamData.updateTeam(team);

                            event.reply(context.localize("command.team.rename.message.done")).setEphemeral(true).queue();
                        });
    }
}
