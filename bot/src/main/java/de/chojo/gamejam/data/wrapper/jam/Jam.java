/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import de.chojo.gamejam.data.wrapper.team.JamTeam;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public record Jam(int id, JamTimes times,
                  JamState state, String topic,
                  List<Long> registrations,
                  List<JamTeam> teams) {

    public void finish(Guild guild) {
        state.active(false);
        state.voting(false);
        state.ended(true);

        for (var team : teams()) {
            team.delete(guild);
        }
    }
}
