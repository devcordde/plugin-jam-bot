/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class OrgaRole implements SubCommand<JamSettings> {
    private final GuildData jamData;

    public OrgaRole(GuildData jamData) {
        this.jamData = jamData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var guildSettings = jamData.getSettings(event.getGuild()).join();
        guildSettings.orgaRole(event.getOption("role").getAsRole().getIdLong());
        jamData.updateSettings(guildSettings)
                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
    }
}
