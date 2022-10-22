/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.settings.handler.Info;
import de.chojo.gamejam.commands.settings.handler.JamRole;
import de.chojo.gamejam.commands.settings.handler.Locale;
import de.chojo.gamejam.commands.settings.handler.TeamSize;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Settings extends SlashCommand {
    public Settings(JamData jamData, GuildData guildData) {
        super(Slash.of("settings", "command.settings.description")
                .adminCommand()
                .subCommand(SubCommand.of("jamrole", "command.settings.jamRole.description")
                        .handler(new JamRole(jamData))
                        .argument(Argument.role("role", "command.settings.jamRole.arg.role").asRequired()))
                .subCommand(SubCommand.of("teamsize", "command.settings.teamSize.description")
                        .handler(new TeamSize(jamData))
                        .argument(Argument.integer("size", "command.settings.teamSize.arg.size").asRequired()))
                .subCommand(SubCommand.of("orgarole", "command.settings.orgaRole.description")
                        // TODO: Command gone?
                        .handler(null)
                        .argument(Argument.role("role", "command.settings.orgaRole.arg.role").asRequired()))
                .subCommand(SubCommand.of("locale", "command.settings.locale.description")
                        .handler(new Locale(guildData))
                        .argument(Argument.text("locale", "command.settings.locale.arg.locale").asRequired()))
                .subCommand(SubCommand.of("info", "command.settings.info.description")
                        .handler(new Info(jamData, guildData)))
        );
    }
}
