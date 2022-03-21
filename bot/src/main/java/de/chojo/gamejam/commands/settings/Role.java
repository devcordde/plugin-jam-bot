/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Role implements SubCommand<JamSettings> {
    private final JamData jamData;

    public Role(JamData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        settings.jamRole(event.getOption("role").getAsRole().getIdLong());
        jamData.updateJamSettings(event.getGuild(), settings)
                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
    }
}
