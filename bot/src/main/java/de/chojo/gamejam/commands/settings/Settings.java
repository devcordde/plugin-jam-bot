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
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Settings extends SlashCommand {
    public Settings(Guilds guilds) {
        super(Slash.of("settings", "command.settings.description")
                .adminCommand()
                .subCommand(SubCommand.of("jamrole", "command.settings.jamrole.description")
                        .handler(new JamRole(guilds))
                        .argument(Argument.role("role", "command.settings.jamrole.options.role.description").asRequired()))
                .subCommand(SubCommand.of("teamsize", "command.settings.teamsize.description")
                        .handler(new TeamSize(guilds))
                        .argument(Argument.integer("size", "command.settings.teamsize.options.size.description").asRequired()))
                .subCommand(SubCommand.of("orgarole", "command.settings.orgarole.description")
                        // TODO: Command gone?
                        .handler(null)
                        .argument(Argument.role("role", "command.settings.orgarole.options.role.description").asRequired()))
                .subCommand(SubCommand.of("locale", "command.settings.locale.description")
                        .handler(new Locale(guilds))
                        .argument(Argument.text("locale", "command.settings.locale.options.locale.description").asRequired()))
                .subCommand(SubCommand.of("info", "command.settings.info.description")
                        .handler(new Info(guilds)))
        );
    }
}
