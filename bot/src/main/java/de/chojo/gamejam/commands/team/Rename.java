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

public class Rename implements SubCommand<Jam> {
    private final TeamData teamData;

    public Rename(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        teamData.getTeamByName(jam, event.getOption("name").getAsString())
                .thenAccept(optTeam -> {
                    if (optTeam.isPresent()) {
                        event.reply(context.localize("command.team.create.nameTaken")).setEphemeral(true).queue();
                        return;
                    }
                    var optCurrTeam = teamData.getTeamByMember(jam, event.getUser()).join();

                    if (optCurrTeam.isEmpty()) {
                        event.reply(context.localize("error.noTeam")).setEphemeral(true).queue();
                        return;
                    }

                    var team = optCurrTeam.get();

                    if (team.leader() != event.getUser().getIdLong()) {
                        event.reply(context.localize("error.noLeader")).setEphemeral(true).queue();
                        return;
                    }

                    team.rename(event.getGuild(), event.getOption("name").getAsString());
                    teamData.updateTeam(team).thenRun(() -> event.reply(context.localize("command.team.rename.done")).setEphemeral(true).queue());
                });
    }
}
