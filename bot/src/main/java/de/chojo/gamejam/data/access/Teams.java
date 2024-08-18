/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.access;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

public class Teams {
    private static final Logger log = getLogger(Teams.class);
    private final Guilds guilds;
    private final ShardManager shardManager;

    public Teams(Guilds guilds, ShardManager shardManager) {
        this.guilds = guilds;
        this.shardManager = shardManager;
    }

    public Optional<Team> byId(int id) {
        return query("""
                SELECT guild_id
                FROM team t
                LEFT JOIN jam j ON t.jam_id = j.id
                WHERE t.id = ?
                """)
                .single(call().bind(id))
                .map(row -> {
                    var guildId = row.getLong("guild_id");
                    var guildById = shardManager.getGuildById(guildId);
                    if (guildById == null) {
                        log.warn("Could not find guild with id {}", guildId);
                        return guilds.guild(guildId).teams().byId(id).orElse(null);
                    }
                    return guilds.guild(guildById).teams().byId(id).orElse(null);
                }).first();
    }
}
