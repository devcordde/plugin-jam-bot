/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin.handler.votes;

import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class ChangeVotes implements SlashHandler {
    private final JamData jamData;
    private final boolean voting;
    private final String content;

    public ChangeVotes(JamData jamData, boolean voting, String content) {
        this.jamData = jamData;
        this.voting = voting;
        this.content = content;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        jamData.getActiveJam(event.getGuild())
               .ifPresentOrElse(
                       jam -> {
                           jam.state().voting(voting);
                           jamData.updateJamState(jam);
                           event.reply(context.localize(content)).queue();
                       },
                       () -> event.reply(context.localize("error.noActiveJam")).setEphemeral(true).queue());
    }
}
