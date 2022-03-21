/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.commands.settings.Info;
import de.chojo.gamejam.commands.settings.Locale;
import de.chojo.gamejam.commands.settings.OrgaRole;
import de.chojo.gamejam.commands.settings.Role;
import de.chojo.gamejam.commands.settings.TeamSize;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.util.MapBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Map;

public class Settings extends SimpleCommand {
    private final JamData jamData;

    private final Map<String, SubCommand<JamSettings>> settingsSubcommandMap;

    public Settings(JamData jamData, GuildData guildData, ILocalizer localizer) {
        super(CommandMeta.builder("settings", "manage bot settings")
                .withPermission()
                .addSubCommand("role", "Set the role which will be assigned to registered members.",
                        argsBuilder().add(SimpleArgument.role("role", "The role to assign after registration").asRequired()).build())
                .addSubCommand("team_size", "Define the max team size.",
                        argsBuilder().add(SimpleArgument.integer("size", "The max team size").asRequired()).build())
                .addSubCommand("orga_role", "Define the organisation team role.",
                        argsBuilder().add(SimpleArgument.integer("role", "The role which can manage the bot").asRequired()).build())
                .addSubCommand("locale", "Change the bot language.",
                        argsBuilder().add(SimpleArgument.integer("locale", "The required language").asRequired()).build())
                .addSubCommand("info", "Show the current settings")
                .build());
        this.jamData = jamData;
        settingsSubcommandMap = new MapBuilder<String, SubCommand<JamSettings>>()
                .add("role", new Role(jamData))
                .add("team_size", new TeamSize(jamData))
                .add("orga_role", new OrgaRole(guildData))
                .add("locale", new Locale(guildData, localizer))
                .add("info", new Info(guildData))
                .build();
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getJamSettings(event.getGuild())
                .thenAccept(settings -> {
                    var subcommand = settingsSubcommandMap.get(event.getSubcommandName());
                    if (subcommand != null) {
                        subcommand.execute(event, context, settings);
                    }
                }).whenComplete(Future.error());
    }
}
