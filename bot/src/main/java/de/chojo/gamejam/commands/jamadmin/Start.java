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
            var nextJam = jamData.getNextOrCurrentJam(event.getGuild()).join();
            if (nextJam.isEmpty()) {
                event.reply("There is no upcoming jam.").queue();
                return;
            }
            nextJam.get().state().active(true);
            jamData.updateJamState(jam.get());
            event.reply("Jam state changed to active").queue();
        }).whenComplete(Future.error());
    }

    public JamData jamData() {
        return jamData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Start) obj;
        return Objects.equals(this.jamData, that.jamData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jamData);
    }

    @Override
    public String toString() {
        return "Start[" +
                "jamData=" + jamData + ']';
    }

}
