/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.data.access.Guilds;
import net.dv8tion.jda.api.entities.Member;

public class CommandContextProvider {
    private final Guilds guilds;

    public CommandContextProvider(Guilds guilds) {
        this.guilds = guilds;
    }

    public UserContext getUserContext(Member member) {
        var jamOpt = guilds.guild(member.getGuild()).jams().activeJam();

        if (jamOpt.isEmpty()) {
            return new UserContext(null);
        }

        var jam = jamOpt.get();
        var team = jam.teams().byMember(member);
        return team.map(UserContext::new).orElseGet(() -> new UserContext(null));

    }

    public Guilds guilds() {
        return guilds;
    }
}
