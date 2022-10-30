/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.exceptions.ThrowingConsumer;
import de.chojo.sadu.wrapper.util.ParamBuilder;

import java.sql.SQLException;

public class Settings extends QueryFactory {
    private final long guildId;
    private String locale = "en_US";
    private long orgaRole = 0;

    public Settings(QueryFactory queryFactory, long guildId) {
        super(queryFactory);
        this.guildId = guildId;
    }

    public Settings(QueryFactory queryFactory, long guildId, String locale, long orgaRole) {
        super(queryFactory);
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
        if (set("locale", stmt -> stmt.setString(locale))) {
            this.locale = locale;
        }
    }

    public void orgaRole(long orgaRole) {
        if (set("manager_role", stmt -> stmt.setLong(orgaRole))) {
            this.orgaRole = orgaRole;
        }
    }

    public Long guildId() {
        return guildId;
    }

    private boolean set(String column, ThrowingConsumer<ParamBuilder, SQLException> stmt) {
        return builder()
                .query("""
                       INSERT INTO settings(guild_id, %s) VALUES(?,?)
                       ON CONFLICT(guild_id)
                           DO UPDATE
                               SET %s = excluded.%s
                       """, column, column, column)
                .parameter(p -> {
                    p.setLong(guildId());
                    stmt.accept(p);
                })
                .update()
                .sendSync()
                .changed();
    }
}
