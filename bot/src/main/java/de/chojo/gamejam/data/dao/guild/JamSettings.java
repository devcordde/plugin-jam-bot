/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Role;

import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class JamSettings {
    private final JamGuild jamGuild;
    private int teamSize;
    private long jamRole;

    public JamSettings(JamGuild jamGuild) {
        this(jamGuild, 4, 0);
    }

    public JamSettings(JamGuild jamGuild, int teamSize, long jamRole) {
        this.jamGuild = jamGuild;
        this.teamSize = teamSize;
        this.jamRole = jamRole;
    }

    public int teamSize() {
        return teamSize;
    }

    public long jamRole() {
        return jamRole;
    }

    public void teamSize(int teamSize) {
        if (set("team_size", stmt -> stmt.bind(teamSize))) {
            this.teamSize = teamSize;
        }
    }

    public void jamRole(Role jamRole) {
        if (set("jam_role", stmt -> stmt.bind(jamRole.getIdLong()))) {
            this.jamRole = jamRole.getIdLong();
        }
    }

    private boolean set(String column, Function<Call, Call> stmt) {
        return query("""
                INSERT INTO jam_settings(guild_id, %s) VALUES(?,?)
                ON CONFLICT(guild_id)
                    DO UPDATE
                        SET %s = excluded.%s
                """, column, column, column)
                .single(stmt.apply(call().bind(jamGuild.guildId())))
                .update()
                .changed();
    }
}
