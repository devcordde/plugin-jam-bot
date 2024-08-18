/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.Settings;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Locale implements SlashHandler {
    private final Guilds guilds;

    public Locale(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guilds.guild(event).settings();
        var locale = event.getOption("locale").getAsString();
        context.guildLocalizer().localizer().getLanguage(locale)
               .ifPresentOrElse(language -> {
                   settings.locale(language.getLocale());
                   event.reply(context.localize("command.settings.locale.message.updated")).setEphemeral(true).queue();
                   context.interactionHub().refreshGuildCommands(event.getGuild());
               }, () -> event.reply(context.localize("command.settings.locale.message.invalid")).setEphemeral(true).queue());

    }
}
