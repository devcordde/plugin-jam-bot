/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.web;

import de.chojo.pluginjam.PluginJam;
import de.chojo.pluginjam.servers.ServerRegistry;
import de.chojo.pluginjam.web.server.Server;
import io.javalin.Javalin;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final Server server;
    private Javalin javalin;

    private Api(ServerRegistry registry) {
        this.server = new Server(registry);
    }

    public static Api create(ServerRegistry registry) {
        var api = new Api(registry);
        api.ignite();
        return api;
    }

    private void ignite() {
        var classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PluginJam.class.getClassLoader());
        javalin = Javalin.create(config -> {
            config.concurrency.useVirtualThreads = true;
            config.routes.apiBuilder(this::routes);
        });
        int port = Integer.parseInt(System.getProperty("javalin.port", "30000"));
        javalin.start("0.0.0.0", port);
        Thread.currentThread().setContextClassLoader(classLoader);

    }

    private void routes() {
        before(ctx -> log.debug("Received request on {}.", ctx.path()));
        path("v1", server::buildRoutes);
    }
}
