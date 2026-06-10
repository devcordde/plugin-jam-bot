/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api;

import de.chojo.pluginjam.PluginJam;
import de.chojo.pluginjam.api.routes.ConfigurationRoute;
import de.chojo.pluginjam.api.routes.OnlineRoute;
import de.chojo.pluginjam.api.routes.StatsRoute;
import de.chojo.pluginjam.service.ServerRequests;
import io.javalin.Javalin;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final ConfigurationRoute configurationRoute;
    private final StatsRoute statsRoute;
    private final OnlineRoute onlineRoute;

    private Api(Plugin plugin, ServerRequests serverRequests) {
        configurationRoute = new ConfigurationRoute(plugin);
        statsRoute = new StatsRoute(plugin);
        onlineRoute = new OnlineRoute();
    }

    public static Api create(Plugin plugin, ServerRequests serverRequests) {
        var api = new Api(plugin, serverRequests);
        api.ignite();
        return api;
    }

    private void ignite() {
        var classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PluginJam.class.getClassLoader());
        Javalin javalin = Javalin.create(config -> {
            config.concurrency.useVirtualThreads = true;
            config.routes.apiBuilder(this::routes);
        });
        javalin.start("0.0.0.0", Integer.parseInt(System.getProperty("javalin.port", "30000")));
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void routes() {
        before(ctx -> log.debug("Received request on {}.", ctx.path()));
        path("v1", configurationRoute::buildRoutes);
        path("v1", statsRoute::buildRoutes);
        path("v1", onlineRoute::buildRoutes);
    }
}
