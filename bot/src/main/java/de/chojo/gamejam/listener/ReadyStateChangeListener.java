/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.listener;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyStateChangeListener {

    private final ShardManager shardManager;
    private static final Logger log = LoggerFactory.getLogger(ReadyStateChangeListener.class);

    public ReadyStateChangeListener(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    @SubscribeEvent
    public void finishedLoading(ReadyEvent readyEvent){
        log.warn("Available Guilds: {}",readyEvent.getGuildAvailableCount());
        shardManager.setActivity(Activity.playing("Plugin-Jam-Bot"));
        for (Guild guild : shardManager.getGuilds()) {
            guild.loadMembers(member -> {
                log.warn("Loaded member \"{}\" of guild \"{}\"", member.getEffectiveName(), guild.getName());
            });
        }
    }

    @SubscribeEvent
    public void shutdown(ShutdownEvent shutdownEvent){
        shardManager.setActivity(null);
    }
}
