/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class JamAdmin extends SimpleCommand {

    private final JamData jamData;
    public JamAdmin(JamData jamData) {
        super(CommandMeta.builder("jamadmin", "Manage jams")
                .withPermission()
                .addSubCommand("create", "Create a new game jam",
                        argsBuilder()
                                .add(SimpleArgument.string("topic", "The topic of the game jam").asRequired())
                                .add(SimpleArgument.string("topic_tagline", "Topic tagline as an addition to the topic").asRequired())
                                .add(SimpleArgument.string("timezone", "The timezone of the game jam. \"Europe/Berlin\" for example.").asRequired())
                                .add(SimpleArgument.string("register_start", "Registrations opening. Format: YYYY.MM.DD hh:mm").asRequired())
                                .add(SimpleArgument.string("register_end", "Registrations close. Format: YYYY.MM.DD hh:mm").asRequired())
                                .add(SimpleArgument.string("jam_start", "Game Jam start. Format: YYYY.MM.DD hh:mm").asRequired())
                                .add(SimpleArgument.string("jam_end", "Game Jam end. Format: YYYY.MM.DD hh:mm").asRequired())
                                .build())
                .addSubCommand("start_jam", "start the next scheduled jam")
                .addSubCommand("end_jam", "Ends the currently active jam")
                .addSubCommand("open_votes", "Open votes for the current active jam")
                .addSubCommand("close_votes", "close votes for the current active jam")
                .build());
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {

    }
}
