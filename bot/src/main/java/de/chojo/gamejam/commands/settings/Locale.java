/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record Locale(GuildData guildData, ILocalizer localizer) implements SubCommand<JamSettings> {

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var locale = event.getOption("locale").getAsString();
        var guildSettings = guildData.getSettings(event.getGuild()).join();
        var lang = localizer.languages().stream().filter(l -> l.isLanguage(locale)).findFirst();
        if (lang.isEmpty()) {
            event.reply("Invalid locale").setEphemeral(true).queue();
            return;
        }
        guildSettings.locale(lang.get().getCode());
        guildData.updateSettings(guildSettings)
                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
    }
}
