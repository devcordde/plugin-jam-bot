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
}
