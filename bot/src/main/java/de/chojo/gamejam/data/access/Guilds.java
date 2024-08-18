/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.access;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.gamejam.data.dao.JamGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.Interaction;

import javax.sql.DataSource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Guilds {
    private final Cache<Long, JamGuild> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    private final DataSource dataSource;

    public Guilds(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JamGuild guild(Interaction interaction) {
        return guild(interaction.getGuild());
    }

    public JamGuild guild(Guild guild) {
        try {
            return cache.get(guild.getIdLong(), () -> new JamGuild(guild)).refresh(guild);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    public JamGuild guild(long guild) {
        try {
            return cache.get(guild, () -> new JamGuild(guild));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
