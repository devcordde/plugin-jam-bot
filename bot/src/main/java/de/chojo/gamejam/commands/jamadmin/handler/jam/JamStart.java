/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin.handler.jam;

import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class JamStart implements SlashHandler {
    private final JamData jamData;

    public JamStart(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        jamData.getActiveJam(event.getGuild()).ifPresentOrElse(
                jam -> event.reply(context.localize("error.alreadyActive")).setEphemeral(true).queue(),
                () -> jamData.getNextOrCurrentJam(event.getGuild())
                             .ifPresentOrElse(
                                     next -> {
                                         next.state().active(true);
                                         jamData.updateJamState(next);
                                         event.reply(context.localize("command.start.message.activated")).setEphemeral(true)
                                              .queue();
                                     },
                                     () -> event.reply(context.localize("error.noupcomingjam")).setEphemeral(true)
                                                .queue()));
    }
}
