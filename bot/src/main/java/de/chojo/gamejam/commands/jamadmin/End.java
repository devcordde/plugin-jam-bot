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

public final class End implements SubCommand.Nonce {
    private final JamData jamData;

    public End(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply(context.localize("error.noConfirm")).setEphemeral(true).queue();
            return;
        }

        jamData.getActiveJam(event.getGuild()).thenAccept(optJam -> {
            optJam.ifPresentOrElse(jam -> {
                jam.finish(event.getGuild());
                jamData.updateJamState(jam);
                event.reply(context.localize("command.jamAdmin.end.ended")).setEphemeral(true).queue();
            }, () -> event.reply(context.localize("error.noActiveJam")).setEphemeral(true).queue());
        }).whenComplete(Future.error());
    }
}
