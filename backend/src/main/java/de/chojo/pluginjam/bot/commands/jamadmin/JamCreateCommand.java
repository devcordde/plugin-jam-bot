/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.jamadmin;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.placeholder.Entry;
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
public class JamCreateCommand {
    public static final String PATTERN = "yyyy.MM.dd HH:mm";
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern(PATTERN);
    private final CommandContextProvider commandContextProvider;

    @Inject
    public JamCreateCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @io.github.kaktushose.jdac.annotations.interactions.Command(value = "jamadmin create")
    public void onCommand(
            CommandEvent event,
            @Param("topic") String topic,
            @Param("tagline") String tagline,
            @Param("timezone") String timezone,
            @Param("registerstart") String registerStart,
            @Param("registerend") String registerEnd,
            @Param("jamstart") String jamStart,
            @Param("jamend") String jamEnd
    ) {
        var guild = event.getGuild();
        ZoneId parsedTimeZone;
        try {
            parsedTimeZone = ZoneId.of(timezone);
        } catch (DateTimeException e) {
            event.reply("error-invalidtimezone");
            return;
        }

        try {
            var registerStartParsed = parseTime(registerStart, parsedTimeZone);
            var registerEndParsed = parseTime(registerEnd, parsedTimeZone);
            var jamStartParsed = parseTime(jamStart, parsedTimeZone);
            var jamEndParsed = parseTime(jamEnd, parsedTimeZone);

            commandContextProvider.pluginJamService().createJam(
                    guild.getIdLong(),
                    topic,
                    tagline,
                    parsedTimeZone.getId(),
                    registerStartParsed,
                    registerEndParsed,
                    jamStartParsed,
                    jamEndParsed
            );
        } catch (DateTimeException e) {
            event.with().ephemeral(true).reply("error-invalidrimeformat", Entry.entry("FORMAT", PATTERN));
            return;
        }
        event.with().ephemeral(true).reply("command-jamadmin-create-message-created");
    }


    private ZonedDateTime parseTime(String time, ZoneId zoneId) throws DateTimeException {
        var parsed = LocalDateTime.from(DATE_PARSER.parse(time));
        return ZonedDateTime.ofInstant(parsed, zoneId.getRules().getOffset(parsed), zoneId);
    }

    @AutoComplete(value = "onCommand", options = "timezone")
    public void onAutoComplete(AutoCompleteEvent event) {
        var value = event.getValue().toLowerCase(Locale.ROOT);
        var choices = ZoneId.getAvailableZoneIds().stream()
                .filter(zone -> zone.toLowerCase(Locale.ROOT).contains(value))
                .limit(25)
                .map(zone -> new Command.Choice(zone, zone))
                .collect(Collectors.toList());
        event.replyChoices(choices);
    }
}
