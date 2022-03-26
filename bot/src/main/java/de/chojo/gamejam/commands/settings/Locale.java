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

public final class Locale implements SubCommand<JamSettings> {
    private final GuildData guildData;
    private final ILocalizer localizer;

    public Locale(GuildData guildData, ILocalizer localizer) {
        this.guildData = guildData;
        this.localizer = localizer;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var locale = event.getOption("locale").getAsString();
        var guildSettings = guildData.getSettings(event.getGuild()).join();
        localizer.languages().stream().filter(l -> l.isLanguage(locale)).findFirst()
                .ifPresentOrElse(language -> {
            guildSettings.locale(language.getCode());
            guildData.updateSettings(guildSettings)
                    .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
        }, () -> event.reply("Invalid locale").setEphemeral(true).queue());
    }
}
