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

public final class JamEnd implements SlashHandler {
    private final JamData jamData;

    public JamEnd(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply(context.localize("error.noConfirm")).setEphemeral(true).queue();
            return;
        }

        jamData.getActiveJam(event.getGuild())
               .ifPresentOrElse(jam -> {
                   jam.finish(event.getGuild());
                   jamData.updateJamState(jam);
                   event.reply(context.localize("command.jamadmin.end.ended")).setEphemeral(true).queue();
               }, () -> event.reply(context.localize("error.noActiveJam")).setEphemeral(true).queue());
    }
}
