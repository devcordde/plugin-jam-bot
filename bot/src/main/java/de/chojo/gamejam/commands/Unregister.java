/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.ZonedDateTime;

public class Unregister extends SimpleCommand {
    private final JamData jamData;
    private final TeamData teamData;

    public Unregister(JamData jamData, TeamData teamData) {
        super(CommandMeta.builder("unregister", "command.unregister.description")
                .build());
        this.jamData = jamData;
        this.teamData = teamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getNextOrCurrentJam(event.getGuild()).thenAccept(optJam -> {
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

            teamData.getTeamByMember(jam, event.getMember()).join().ifPresentOrElse(team -> {
                event.reply(context.localize("command.unregister.inTeam")).queue();
            }, () -> {
                jamData.getJamSettings(event.getGuild()).thenAccept(settings -> {
                    var role = event.getGuild().getRoleById(settings.jamRole());
                    if (role != null) {
                        event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                    }
                    event.reply(context.localize("command.unregister.unregistered"))
                            .setEphemeral(true)
                            .queue();
                }).whenComplete(Future.handleComplete());
            });
        }).whenComplete(Future.handleComplete());
    }
}
