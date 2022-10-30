/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.access;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class Teams extends QueryFactory {
    private static final Logger log = getLogger(Teams.class);
    private final Guilds guilds;
    private final ShardManager shardManager;

    public Teams(DataSource dataSource, Guilds guilds, ShardManager shardManager) {
        super(dataSource);
        this.guilds = guilds;
        this.shardManager = shardManager;
    }

    public Optional<Team> byId(int id) {
        return builder(Team.class)
                .query("""
                       SELECT guild_id
                       FROM team t
                       LEFT JOIN jam j ON t.jam_id = j.id
                       WHERE t.id = ?
                       """)
                .parameter(stmt -> stmt.setInt(id))
                .readRow(row -> {
                    var guildId = row.getLong("guild_id");
                    var guildById = shardManager.getGuildById(guildId);
                    if (guildById == null) {
                        log.warn("Could not find guild with id {}", guildId);
                        return guilds.guild(guildId).teams().byId(id).orElse(null);
                    }
                    return guilds.guild(guildById).teams().byId(id).orElse(null);
                })
                .firstSync();
    }
}
