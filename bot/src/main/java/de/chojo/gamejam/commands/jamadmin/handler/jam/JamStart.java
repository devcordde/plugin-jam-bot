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

public final class JamStart implements SlashHandler {
    private final Guilds guilds;

    public JamStart(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var jams = guilds.guild(event).jams();
        var currJam = jams.activeJam();
        if (currJam.isPresent()) {
            event.reply(context.localize("error.alreadyActive")).setEphemeral(true).queue();
            return;
        }
        var next = jams.nextOrCurrent();
        if (next.isPresent()) {
            next.get().state().active(true);
            event.reply(context.localize("command.start.message.activated"))
                 .setEphemeral(true)
                 .queue();

        }
        event.reply(context.localize("error.noupcomingjam")).setEphemeral(true)
             .queue();
    }
}
