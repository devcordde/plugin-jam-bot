/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.exceptions.ThrowingConsumer;
import de.chojo.sadu.wrapper.util.ParamBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.sql.SQLException;

public class JamSettings extends QueryFactory {
    private final JamGuild jamGuild;
    private int teamSize;
    private long jamRole;

    public JamSettings(JamGuild jamGuild) {
        this(jamGuild, 4, 0);
    }

    public JamSettings(JamGuild jamGuild, int teamSize, long jamRole) {
        super(jamGuild);
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
        if (set("team_size", stmt -> stmt.setInt(teamSize))) {
            this.teamSize = teamSize;
        }
    }

    public void jamRole(Role jamRole) {
        if (set("jam_role", stmt -> stmt.setLong(jamRole.getIdLong()))) {
            this.jamRole = jamRole.getIdLong();
        }
    }

    private boolean set(String column, ThrowingConsumer<ParamBuilder, SQLException> stmt) {
        return builder()
                .query("""
                       INSERT INTO jam_settings(guild_id, %s) VALUES(?,?)
                       ON CONFLICT(guild_id)
                           DO UPDATE
                               SET %s = excluded.%s
                       """, column, column, column)
                .parameter(p -> {
                    p.setLong(jamGuild.guildId());
                    stmt.accept(p);
                })
                .update()
                .sendSync()
                .changed();
    }
}
