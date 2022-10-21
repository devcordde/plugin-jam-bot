/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.web;

import com.velocitypowered.api.proxy.ProxyServer;
import de.chojo.pluginjam.configuration.Configuration;
import de.chojo.pluginjam.servers.ServerRegistry;
import de.chojo.pluginjam.web.server.Server;
import io.javalin.Javalin;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private final Javalin javalin;
    private final Server server;

    private Api(ServerRegistry registry, Javalin javalin) {
        this.javalin = javalin;
        this.server = new Server(registry);
    }

    public static Api create(Configuration configuration, ServerRegistry registry) {
        var javalin = Javalin.create();
        javalin.start(configuration.api().host(), configuration.api().port());
        var api = new Api(registry, javalin);
        api.ignite();
        return api;
    }

    private void ignite() {
        javalin.routes(() -> {
            before(ctx -> log.debug("Received request on {}.", ctx.path()));
            path("v1", server::buildRoutes);
        });
    }
}
