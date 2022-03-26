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
import de.chojo.jdautil.util.MapBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JamAdmin extends SimpleCommand {

    private final Map<String, SubCommand.Nonce> subCommandMap;

    public JamAdmin(JamData jamData) {
        super(CommandMeta.builder("jamadmin", "command.jamAdmin.description")
                .withPermission()
                .addSubCommand("create", "command.jamAdmin.create.description",
                        argsBuilder()
                                .add(SimpleArgument.string("topic", "command.jamAdmin.create.arg.topic").asRequired().withAutoComplete())
                                .add(SimpleArgument.string("topic-tagline", "command.jamAdmin.create.arg.topic-tagline").asRequired())
                                .add(SimpleArgument.string("timezone", "command.jamAdmin.create.arg.timezone").asRequired())
                                .add(SimpleArgument.string("register-start", formatArg("command.jamAdmin.create.arg.register-start")).asRequired())
                                .add(SimpleArgument.string("register-end", formatArg("command.jamAdmin.create.arg.register-end")).asRequired())
                                .add(SimpleArgument.string("jam-start", formatArg("command.jamAdmin.create.arg.jam-start")).asRequired())
                                .add(SimpleArgument.string("jam-end", formatArg("command.jamAdmin.create.arg.jam-end")).asRequired())
                                .build())
                .addSubCommand("start-jam", "command.jamAdmin.start-jam.description")
                .addSubCommand("end-jam", "command.jamAdmin.end-jam.description",
                        argsBuilder().add(SimpleArgument.bool("confirm", "Set to true to confirm").asRequired()).build())
                .addSubCommand("open-votes", "command.jamAdmin.open-votes.description")
                .addSubCommand("close-votes", "command.jamAdmin.close-votes.description")
                .build());
        subCommandMap = new MapBuilder<String, SubCommand.Nonce>()
                .add("create", new Create(jamData))
                .add("start-jam", new Start(jamData))
                .add("end-jam", new End(jamData))
                .add("open-votes", new ChangeVotes(jamData, true, "command.jamAdmin.vote.open"))
                .add("close-votes", new ChangeVotes(jamData, false, "command.jamAdmin.vote.close"))
                .build();
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

    private static String formatArg(String key) {
        return String.format("$%s$ $%s$: %s", key, "command.jamAdmin.create.arg.format", Create.PATTERN);
    }
}
