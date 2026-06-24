/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.jamadmin;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import de.chojo.pluginjam.bot.message.EmbedHelper;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

@Bundle("locale")
@Interaction
public class JamListCommand {
    public static final String PATTERN = "yyyy.MM.dd HH:mm";
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern(PATTERN);
    private final CommandContextProvider commandContextProvider;
    private final MessageResolver messageResolver;

    @Inject
    public JamListCommand(CommandContextProvider commandContextProvider, MessageResolver messageResolver) {
        this.commandContextProvider = commandContextProvider;
        this.messageResolver = messageResolver;
    }

    @io.github.kaktushose.jdac.annotations.interactions.Command(value = "jamadmin list")
    public void onCommand(CommandEvent event) {
        var jams = commandContextProvider.pluginJamService().getJams(event.getGuild().getIdLong());
        if (jams == null || jams.isEmpty()) {
            event.with().ephemeral(true).reply("command-jamadmin-list-no-jams");
            return;
        }
        var locale = event.getUserLocale();
        var embed = EmbedHelper.buildJamListEmbed(jams, event, messageResolver, locale);
        event.reply(embed);
    }
}
