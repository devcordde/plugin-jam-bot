/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api;

import de.chojo.pluginjam.PluginJam;
import de.chojo.pluginjam.api.routes.Configuration;
import de.chojo.pluginjam.api.routes.Requests;
import de.chojo.pluginjam.api.routes.Stats;
import de.chojo.pluginjam.service.ServerRequests;
import io.javalin.Javalin;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final Javalin javalin;
    private final Configuration configuration;
    private final Stats stats;
    private final Requests requests;

    private Api(Javalin javalin, Plugin plugin, ServerRequests serverRequests) {
        this.javalin = javalin;
        configuration = new Configuration(plugin);
        stats = new Stats(plugin);
        requests = new Requests(serverRequests);
    }

    public static Api create(Plugin plugin, ServerRequests serverRequests) {
        var classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PluginJam.class.getClassLoader());
        var javalin = Javalin.create();
        javalin.start("0.0.0.0", Integer.parseInt(System.getProperty("javalin.port", "30000")));
        Thread.currentThread().setContextClassLoader(classLoader);
        var api = new Api(javalin, plugin, serverRequests);
        api.ignite();
        return api;
    }

    private void ignite() {
        javalin.routes(() -> {
            before(ctx -> log.debug("Received request on {}.", ctx.path()));
            path("v1", configuration::buildRoutes);
            path("v1", stats::buildRoutes);
            path("v1", requests::buildRoutes);
        });
    }
}
