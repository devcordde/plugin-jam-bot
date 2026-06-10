/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.settings;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import de.chojo.pluginjam.bot.message.EmbedHelper;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;

@Bundle("locale")
@Interaction
public final class SettingsInfoCommand {
    private final CommandContextProvider commandContextProvider;
    private final MessageResolver messageResolver;

    @Inject
    public SettingsInfoCommand(CommandContextProvider commandContextProvider, MessageResolver messageResolver) {
        this.commandContextProvider = commandContextProvider;
        this.messageResolver = messageResolver;
    }

    @Command(value = "settings info")
    public void onCommand(CommandEvent event) {
        var guildId = event.getGuild().getIdLong();

        var settings = commandContextProvider.settingsService().getSettings(guildId);
        var embed = EmbedHelper.buildSettingsInfoEmbed(settings, messageResolver, event.getUserLocale());

        event.reply(embed);
    }
}
