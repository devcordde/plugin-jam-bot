/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Settings extends SimpleCommand {
    private final JamData jamData;

    public Settings(JamData jamData) {
        super(CommandMeta.builder("settings", "manage bot settings")
                .withPermission()
                .addSubCommand("role", "Set the role which will be assigned to registered members.",
                        argsBuilder().add(SimpleArgument.role("role", "The role to assign after registration").asRequired()).build())
                .addSubCommand("team_size", "Define the max team size.",
                        argsBuilder().add(SimpleArgument.integer("size", "The max team size").asRequired()).build())
                .addSubCommand("orga_role", "Define the organisation team role.",
                        argsBuilder().add(SimpleArgument.integer("role", "The role which can manage the bot").asRequired()).build())
                .addSubCommand("info", "Show the current settings")
                .build());
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getSettings(event.getGuild())
                .thenAccept(settings -> {
                    if ("role".equals(event.getSubcommandName())) {
                        settings.jamRole(event.getOption("role").getAsRole().getIdLong());
                        jamData.updateSettings(event.getGuild(), settings)
                                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
                        return;
                    }
                    if ("team_size".equals(event.getSubcommandName())) {
                        settings.teamSize(event.getOption("size").getAsInt());
                        jamData.updateSettings(event.getGuild(), settings)
                                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
                        return;
                    }
                    if ("team_size".equals(event.getSubcommandName())) {
                        settings.orgaRole(event.getOption("role").getAsRole().getIdLong());
                        jamData.updateSettings(event.getGuild(), settings)
                                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
                        return;
                    }
                    if ("info".equals(event.getSubcommandName())) {
                        var embed = new LocalizedEmbedBuilder(context.localizer())
                                .setTitle("Settings")
                                .addField("Game Jam Role", MentionUtil.role(settings.jamRole()), true)
                                .addField("Max Team Size", String.valueOf(settings.teamSize()), true)
                                .addField("Orga Role", MentionUtil.role(settings.orgaRole()), true)
                                .build();
                        event.replyEmbeds(embed).setEphemeral(true).queue();
                    }
                }).whenComplete(Future.error());
    }
}
