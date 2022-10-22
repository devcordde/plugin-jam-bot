/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.jam.JamTimes;
import de.chojo.gamejam.data.wrapper.jam.TimeFrame;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

public class Create implements SlashHandler {
    public static final String PATTERN = "yyyy.MM.dd HH:mm";
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern(PATTERN);

    private final JamData jamData;

    public Create(JamData jamData) {
        this.jamData = jamData;
    }

    private ZonedDateTime parseTime(String time, ZoneId zoneId) throws DateTimeException {
        var parsed = LocalDateTime.from(DATE_PARSER.parse(time));
        return ZonedDateTime.ofInstant(parsed, zoneId.getRules().getOffset(parsed), zoneId);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var topic = String.join("\n", event.getOption("topic")
                                           .getAsString(), event.getOption("topic-tagline", "", OptionMapping::getAsString));
        ZoneId timezone;
        try {
            timezone = ZoneId.of(event.getOption("timezone").getAsString());
        } catch (DateTimeException e) {
            event.reply(context.localize("error.invalidTimezone")).setEphemeral(true).queue();
            return;
        }

        var jamBuilder = Jam.create()
                .setTopic(topic);
        try {
            var registerStart = parseTime(event.getOption("register-start").getAsString(), timezone);
            var registerEnd = parseTime(event.getOption("register-end").getAsString(), timezone);
            var jamStart = parseTime(event.getOption("jam-start").getAsString(), timezone);
            var jamEnd = parseTime(event.getOption("jam-end").getAsString(), timezone);
            var times = new JamTimes(timezone, new TimeFrame(registerStart, registerEnd), new TimeFrame(jamStart, jamEnd));
            jamBuilder.setTimes(times);
        } catch (DateTimeException e) {
            event.reply(context.localize("error.invalidTimeFormat", Replacement.create("FORMAT", PATTERN)))
                 .setEphemeral(true).queue();
            return;
        }

        jamData.createJam(jamBuilder.build(), event.getGuild());
        event.reply(context.localize("command.jamadmin.create.created")).setEphemeral(true).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("timezone".equals(event.getFocusedOption().getName())) {
            var value = event.getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            var choices = ZoneId.getAvailableZoneIds().stream()
                                .filter(zone -> zone.toLowerCase(Locale.ROOT).contains(value))
                                .limit(25)
                                .map(zone -> new Command.Choice(zone, zone))
                                .collect(Collectors.toList());
            event.replyChoices(choices).queue();
        }
    }
}
