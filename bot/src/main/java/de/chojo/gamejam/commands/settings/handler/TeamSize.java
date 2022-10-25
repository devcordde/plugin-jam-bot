/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class TeamSize implements SlashHandler {
    private final Guilds guilds;

    public TeamSize(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guilds.guild(event).jamSettings();
        settings.teamSize(event.getOption("size").getAsInt());
        event.reply(context.localize("command.settings.teamsize.message.updated")).setEphemeral(true).queue();

    }
}
