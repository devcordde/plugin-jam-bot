/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.unregister;

import de.chojo.gamejam.commands.unregister.handler.Handler;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

public class Unregister extends SlashCommand {
    private final JamData jamData;
    private final TeamData teamData;

    public Unregister(JamData jamData, TeamData teamData) {
        super(Slash.of("unregister", "command.unregister.description")
                .command(new Handler(jamData, teamData)));
        this.jamData = jamData;
        this.teamData = teamData;
    }
}
