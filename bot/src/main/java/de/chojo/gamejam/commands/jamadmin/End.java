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

import java.util.Objects;

public final class End implements SubCommand.Nonce {
    private final JamData jamData;

    public End(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply("Please confirm").queue();
            return;
        }

        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isEmpty()) {
                event.reply("There is no active jam.").queue();
                return;
            }
            jam.get().finish(event.getGuild());
            jamData.updateJamState(jam.get());
            event.reply("Jam ended.").queue();

        }).whenComplete(Future.error());
    }

    public JamData jamData() {
        return jamData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (End) obj;
        return Objects.equals(this.jamData, that.jamData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jamData);
    }

    @Override
    public String toString() {
        return "End[" +
                "jamData=" + jamData + ']';
    }

}
