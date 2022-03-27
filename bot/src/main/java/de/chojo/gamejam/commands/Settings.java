/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands;

import de.chojo.gamejam.commands.settings.Info;
import de.chojo.gamejam.commands.settings.JamRole;
import de.chojo.gamejam.commands.settings.Locale;
import de.chojo.gamejam.commands.settings.OrgaRole;
import de.chojo.gamejam.commands.settings.TeamSize;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.util.MapBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Map;

public class Settings extends SimpleCommand {
    private final JamData jamData;
    private final OrgaRole orgaRole;
    private final Locale locale;

    private final Map<String, SubCommand<JamSettings>> subCommandMap;

    public Settings(JamData jamData, GuildData guildData) {
        super(CommandMeta.builder("settings", "command.settings.description")
                .withPermission()
                .addSubCommand("jam_role", "command.settings.jamRole.description",
                        argsBuilder().add(SimpleArgument.role("role", "command.settings.jamRole.arg.role").asRequired()).build())
                .addSubCommand("team_size", "command.settings.teamSize.description",
                        argsBuilder().add(SimpleArgument.integer("size", "command.settings.teamSize.arg.size").asRequired()).build())
                .addSubCommand("orga_role", "command.settings.orgaRole.description",
                        argsBuilder().add(SimpleArgument.role("role", "command.settings.orgaRole.arg.role").asRequired()).build())
                .addSubCommand("locale", "command.settings.locale.description",
                        argsBuilder().add(SimpleArgument.string("locale", "command.settings.locale.arg.locale").asRequired()).build())
                .addSubCommand("info", "command.settings.info.description")
                .build());
        this.jamData = jamData;
        orgaRole = new OrgaRole(guildData);
        locale = new Locale(guildData);
        subCommandMap = new MapBuilder<String, SubCommand<JamSettings>>()
                .add("jam_role", new JamRole(jamData))
                .add("team_size", new TeamSize(jamData))
                .add("orga_role", orgaRole)
                .add("locale", locale)
                .add("info", new Info(guildData))
                .build();
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getJamSettings(event.getGuild())
                .thenAccept(settings -> {
                    var subcommand = subCommandMap.get(event.getSubcommandName());
                    if (subcommand != null) {
                        subcommand.execute(event, context, settings);
                    }
                }).whenComplete(Future.error());
    }

    public void init(CommandHub<?> commandHub) {
        orgaRole.setCommandHub(commandHub);
        locale.setCommandHub(commandHub);
    }
}
