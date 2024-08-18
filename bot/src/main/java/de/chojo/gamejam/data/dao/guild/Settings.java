/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.sadu.queries.api.call.Call;

import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Settings {
    private final long guildId;
    private String locale = "en_US";
    private long orgaRole = 0;

    public Settings(long guildId) {
        this.guildId = guildId;
    }

    public Settings(long guildId, String locale, long orgaRole) {
        this.guildId = guildId;
        this.locale = locale;
        this.orgaRole = orgaRole;
    }

    public String locale() {
        return locale;
    }

    public long orgaRole() {
        return orgaRole;
    }

    public void locale(String locale) {
        if (set("locale", stmt -> stmt.bind(locale))) {
            this.locale = locale;
        }
    }

    public void orgaRole(long orgaRole) {
        if (set("manager_role", stmt -> stmt.bind(orgaRole))) {
            this.orgaRole = orgaRole;
        }
    }

    public Long guildId() {
        return guildId;
    }

    private boolean set(String column, Function<Call, Call> stmt) {
        return query("""
                INSERT INTO settings(guild_id, %s) VALUES(?,?)
                ON CONFLICT(guild_id)
                    DO UPDATE
                        SET %s = excluded.%s
                """, column, column, column)
                .single(stmt.apply(call().bind(guildId())))
                .update()
                .changed();
    }
}
