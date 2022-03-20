/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.commands.jamadmin.ChangeVotes;
import de.chojo.gamejam.commands.jamadmin.Create;
import de.chojo.gamejam.commands.jamadmin.End;
import de.chojo.gamejam.commands.jamadmin.Start;
import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JamAdmin extends SimpleCommand {

    private final Map<String, SubCommand> subCommandMap = new HashMap<>();

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
        this.subCommandMap.put("create", new Create(jamData));
        this.subCommandMap.put("start-jam", new Start(jamData));
        this.subCommandMap.put("end-jam", new End(jamData));
        this.subCommandMap.put("open-votes", new ChangeVotes(jamData, true, "Votes opened for current jam."));
        this.subCommandMap.put("close-votes", new ChangeVotes(jamData, false, "Votes closed for current jam."));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var label = event.getSubcommandName();
        var subCommand = subCommandMap.get(label);
        if (subCommand != null) {
            subCommand.execute(event, context);
        }
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
