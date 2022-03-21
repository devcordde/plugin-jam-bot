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

public final class ChangeVotes implements SubCommand.Nonce {
    private final JamData jamData;
    private final boolean voting;
    private final String content;

    public ChangeVotes(JamData jamData, boolean voting, String content) {
        this.jamData = jamData;
        this.voting = voting;
        this.content = content;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isEmpty()) {
                event.reply("There is no active jam.").queue();
                return;
            }
            jam.get().state().voting(voting);
            jamData.updateJamState(jam.get());
            event.reply(content).queue();
        }).whenComplete(Future.error());
    }

    public JamData jamData() {
        return jamData;
    }

    public boolean voting() {
        return voting;
    }

    public String content() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChangeVotes) obj;
        return Objects.equals(this.jamData, that.jamData) &&
                this.voting == that.voting &&
                Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jamData, voting, content);
    }

    @Override
    public String toString() {
        return "ChangeVotes[" +
                "jamData=" + jamData + ", " +
                "voting=" + voting + ", " +
                "content=" + content + ']';
    }

}
