/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.unregister;

import de.chojo.gamejam.commands.unregister.handler.Handler;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Unregister extends SlashCommand {
    public Unregister(Guilds guilds) {
        super(Slash.of("unregister", "command.unregister.description")
                .command(new Handler(guilds)));
    }
}
