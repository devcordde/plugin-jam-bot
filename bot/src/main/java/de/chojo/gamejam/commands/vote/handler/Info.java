/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class Info implements SlashHandler {
    private final Guilds guilds;

    public Info(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        var voteEntries = jam.user(event.getMember()).votes();
        var given = voteEntries.stream()
                               .filter(e -> e.points() != 0)
                               .map(e -> e.team().meta().name() + ": **" + e.points() + "**")
                               .collect(Collectors.joining("\n"));

        var build = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.votes.info.embed.title")
                .setDescription(given)
                .build();
        event.replyEmbeds(build).setEphemeral(true).queue();

    }
}
