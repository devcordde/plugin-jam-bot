/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class Info implements SubCommand<Jam> {
    private final TeamData teamData;

    public Info(TeamData teamData) {
        this.teamData = teamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam) {
        teamData.votesByUser(event.getMember(), jam)
                .whenComplete(Future.handle(voteEntries -> {
                    var given = voteEntries.stream()
                            .filter(e -> e.points() != 0)
                            .map(e -> e.team().name() + ": **" + e.points() + "**")
                            .collect(Collectors.joining("\n"));

                    var build = new LocalizedEmbedBuilder(context.localizer())
                            .setTitle("command.votes.info.title")
                            .setDescription(given)
                            .build();
                    event.replyEmbeds(build).setEphemeral(true).queue();
                }, Future.error()));

    }
}
