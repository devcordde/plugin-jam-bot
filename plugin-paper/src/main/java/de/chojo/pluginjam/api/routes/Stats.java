/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api.routes;

import de.chojo.pluginjam.payload.StatsPayload;
import org.bukkit.plugin.Plugin;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Stats {
    private static final int MB = 1024 * 1024;
    private final Plugin plugin;

    public Stats(Plugin plugin) {
        this.plugin = plugin;
    }

    public void buildRoutes() {
        path("stats", () -> {
            get("", ctx -> {
                var server = plugin.getServer();
                var tps = server.getTPS();
                var avgTickTime = server.getAverageTickTime();
                var onlinePlayers = server.getOnlinePlayers().size();

                var instance = Runtime.getRuntime();
                var total = instance.totalMemory() / MB;
                var free = instance.freeMemory() / MB;
                var used = total - free / MB;
                var max = instance.maxMemory() / MB;
                var activeThreads = Thread.activeCount();

                var statsPayload = new StatsPayload(tps, avgTickTime, onlinePlayers, activeThreads,
                        new StatsPayload.Memory(total, free, used, max));

                ctx.json(statsPayload);
            });
        });
    }
}
