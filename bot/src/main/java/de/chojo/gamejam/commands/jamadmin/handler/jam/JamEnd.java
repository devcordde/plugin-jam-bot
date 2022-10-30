/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin.handler.jam;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class JamEnd implements SlashHandler {
    private final Guilds guilds;

    public JamEnd(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply(context.localize("error.noconfirm")).setEphemeral(true).queue();
            return;
        }


        guilds.guild(event).jams().activeJam()
              .ifPresentOrElse(
                      jam -> {
                          jam.state().finish();
                          event.reply(context.localize("command.jamadmin.jam.end.message.ended")).setEphemeral(true)
                               .queue();
                      },
                      () -> event.reply(context.localize("error.noactivejam")).setEphemeral(true).queue());
    }
}
