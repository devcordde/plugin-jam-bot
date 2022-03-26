/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Start implements SubCommand.Nonce {
    private final JamData jamData;

    public Start(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isPresent()) {
                event.reply("A jam is already active.").setEphemeral(true).queue();
                return;
            }
            jamData.getNextOrCurrentJam(event.getGuild()).join()
                    .ifPresentOrElse(next -> {
                next.state().active(true);
                jamData.updateJamState(next);
                event.reply("Jam state changed to active").setEphemeral(true).queue();
            },()-> event.reply("There is no upcoming jam.").setEphemeral(true).queue());
        }).whenComplete(Future.error());
    }
}
