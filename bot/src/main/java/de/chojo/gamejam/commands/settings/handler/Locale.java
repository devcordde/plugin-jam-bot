/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings.handler;

import de.chojo.gamejam.data.GuildData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Locale implements SlashHandler {
    private final GuildData guildData;

    public Locale(GuildData guildData) {
        this.guildData = guildData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var locale = event.getOption("locale").getAsString();
        var guildSettings = guildData.getSettings(event.getGuild());
        context.guildLocalizer().localizer().getLanguage(locale)
               .ifPresentOrElse(language -> {
                   guildSettings.locale(language.getLocale());
                   guildData.updateSettings(guildSettings);
                   event.reply(context.localize("command.settings.locale.message.updated")).setEphemeral(true).queue();
                   context.commandHub().refreshGuildCommands(event.getGuild());
               }, () -> event.reply(context.localize("command.settings.locale.message.invalid")).setEphemeral(true).queue());

    }
}
