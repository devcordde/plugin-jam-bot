/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.commands.vote.Info;
import de.chojo.gamejam.commands.vote.Ranking;
import de.chojo.gamejam.commands.vote.Vote;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.util.MapBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;

public class Votes extends SimpleCommand {
    private final JamData jamData;
    private final Map<String, SubCommand<Jam>> subcommands;

    public Votes(JamData jamData, TeamData teamData) {
        super(CommandMeta.builder("votes", "Vote for teams")
                .addSubCommand("vote", "vote for teams",
                        argsBuilder()
                                .add(SimpleArgument.string("team", "Name of the team").asRequired().withAutoComplete())
                                .add(SimpleArgument.integer("points", "Points to give.").asRequired().withAutoComplete())
                                .build())
                .addSubCommand("info", "Information about your given vote")
                .addSubCommand("ranking", "The current ranking")
                .build());
        this.jamData = jamData;
        subcommands = new MapBuilder<String, SubCommand<Jam>>()
                .add("vote", new Vote(teamData))
                .add("info", new Info(teamData))
                .add("ranking", new Ranking(teamData))
                .build();

    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getNextOrCurrentJam(event.getGuild())
                .thenAccept(optJam -> {
                    if (optJam.isEmpty()) {
                        event.reply(context.localize("command.team.noJamActive")).setEphemeral(true).queue();
                        return;
                    }

                    var subCommand = subcommands.get(event.getSubcommandName());

                    if (subCommand != null) {
                        subCommand.execute(event, context, optJam.get());
                    }
                }).whenComplete(Future.handleComplete());
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        var option = event.getFocusedOption();
        if ("team".equals(option.getName())) {
            jamData.getNextOrCurrentJam(event.getGuild())
                    .whenComplete(((jam, err) -> {
                        if (err != null || jam.isEmpty()) {
                            event.replyChoices(Collections.emptyList()).queue();
                            return;
                        }
                        var teams = jam.get().teams().stream()
                                .filter(team -> team.matchName(option.getValue()))
                                .map(JamTeam::name)
                                .map(team -> new Command.Choice(team, team))
                                .toList();
                        event.replyChoices(teams).queue();
                    }));
        }
        if ("points".equals(option.getName())) {
            event.replyChoices(IntStream.range(0, 6).mapToObj(num -> new Command.Choice(String.valueOf(num), num)).toList()).queue();
        }
    }
}
