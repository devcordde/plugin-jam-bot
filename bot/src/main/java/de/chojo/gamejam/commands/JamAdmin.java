/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.JamBuilder;
import de.chojo.gamejam.data.wrapper.jam.JamTimes;
import de.chojo.gamejam.data.wrapper.jam.TimeFrame;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.util.Guilds;
import de.chojo.jdautil.wrapper.SlashCommandContext;
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

public class JamAdmin extends SimpleCommand {

    private final JamData jamData;
    private final DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public JamAdmin(JamData jamData) {
        super(CommandMeta.builder("jamadmin", "Manage jams")
                .withPermission()
                .addSubCommand("create", "Create a new game jam",
                        argsBuilder()
                                .add(SimpleArgument.string("topic", "The topic of the game jam").asRequired().withAutoComplete())
                                .add(SimpleArgument.string("topic-tagline", "Topic tagline as an addition to the topic").asRequired())
                                .add(SimpleArgument.string("timezone", "The timezone of the game jam. \"Europe/Berlin\" for example.").asRequired())
                                .add(SimpleArgument.string("register-start", "Registrations opening. Format: YYYY.MM.DD hh:mm").asRequired())
                                .add(SimpleArgument.string("register-end", "Registrations close. Format: YYYY.MM.DD hh:mm").asRequired())
                                .add(SimpleArgument.string("jam-start", "Game Jam start. Format: YYYY.MM.DD hh:mm").asRequired())
                                .add(SimpleArgument.string("jam-end", "Game Jam end. Format: YYYY.MM.DD hh:mm").asRequired())
                                .build())
                .addSubCommand("start-jam", "start the next scheduled jam")
                .addSubCommand("end-jam", "Ends the currently active jam. Deletes all roles and channel",
                        argsBuilder().add(SimpleArgument.bool("confirm", "Set to true to confirm").asRequired()).build())
                .addSubCommand("open-votes", "Open votes for the current active jam")
                .addSubCommand("close-votes", "close votes for the current active jam")
                .build());
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var label = event.getSubcommandName();
        if ("create".equals(label)) {
            create(event);
            return;
        }
        if ("start-jam".equals(label)) {
            startJam(event);
            return;
        }
        if ("open-votes".equals(label)) {
            changeVotes(event, true, "Votes opened for current jam.");
            return;
        }
        if ("close-votes".equals(label)) {
            changeVotes(event, false, "Votes closed for current jam.");
            return;
        }
        if ("end-jam".equals(label)) {
            endJam(event);
            return;
        }
    }

    private void create(SlashCommandInteractionEvent event) {
        var topic = String.join("\n", event.getOption("topic").getAsString(), event.getOption("topic-tagline", "", OptionMapping::getAsString));
        ZoneId timezone;
        try {
            timezone = ZoneId.of(event.getOption("timezone").getAsString());
        } catch (DateTimeException e) {
            event.reply("Invalid timezone.").queue();
            return;
        }

        var jamBuilder = new JamBuilder(-1)
                .setTopic(topic);
        try {
            var registerStart = parseTime(event.getOption("register-start").getAsString(), timezone);
            var registerEnd = parseTime(event.getOption("register-end").getAsString(), timezone);
            var jamStart = parseTime(event.getOption("jam-start").getAsString(), timezone);
            var jamEnd = parseTime(event.getOption("jam-end").getAsString(), timezone);
            var times = new JamTimes(timezone, new TimeFrame(registerStart, registerEnd), new TimeFrame(jamStart, jamEnd));
            jamBuilder.setTimes(times);
        } catch (DateTimeException e) {
            event.reply("Invalid time format. Format is YYYY.MM.DD hh:mm.").queue();
            return;
        }

        jamData.createJam(jamBuilder.build(), event.getGuild());
        event.reply("Jam created.").setEphemeral(true).queue();
    }

    private void startJam(SlashCommandInteractionEvent event) {
        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isPresent()) {
                event.reply("A jam is already active.").setEphemeral(true).queue();
                return;
            }
            var nextJam = jamData.getNextOrCurrentJam(event.getGuild()).join();
            if (nextJam.isEmpty()) {
                event.reply("There is no upcoming jam.").queue();
                return;
            }
            nextJam.get().state().active(true);
            jamData.updateJamState(jam.get());
            event.reply("Jam state changed to active").queue();
        }).whenComplete(Future.error());
    }

    private void changeVotes(SlashCommandInteractionEvent event, boolean voting, String content) {
        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isEmpty()) {
                event.reply("There is no active jam.").queue();
                return;
            }
            jam.get().state().voting(voting);
            jamData.updateJamState(jam.get());
            event.reply(content).queue();
        }).whenComplete(Future.error());
    }

    private void endJam(SlashCommandInteractionEvent event) {
        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply("Please confirm").queue();
            return;
        }

        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isEmpty()) {
                event.reply("There is no active jam.").queue();
                return;
            }
            jam.get().finish(event.getGuild());
            jamData.updateJamState(jam.get());
            event.reply("Jam ended.").queue();

        }).whenComplete(Future.error());
    }

    private ZonedDateTime parseTime(String time, ZoneId zoneId) throws DateTimeException {
        var parsed = LocalDateTime.from(dateParser.parse(time));
        return ZonedDateTime.ofInstant(parsed, zoneId.getRules().getOffset(parsed), zoneId);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        if ("timezone".equals(event.getFocusedOption().getName())) {
            var value = event.getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            var choices = ZoneId.getAvailableZoneIds().stream()
                    .filter(zone -> zone.toLowerCase(Locale.ROOT).startsWith(value))
                    .limit(25)
                    .map(zone -> new Command.Choice(zone, zone))
                    .collect(Collectors.toList());
            event.replyChoices(choices).queue();
        }
    }
}
