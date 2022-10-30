/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.register;

import de.chojo.gamejam.commands.register.handler.Handler;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Register extends SlashCommand {
    public Register(Guilds guilds) {
        super(Slash.of("register", "command.register.description")
                .command(new Handler(guilds)));
    }
}
