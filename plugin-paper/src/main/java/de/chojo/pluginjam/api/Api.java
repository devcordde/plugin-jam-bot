/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api;

import de.chojo.pluginjam.PluginJam;
import de.chojo.pluginjam.api.routes.Configuration;
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

    private Api(Javalin javalin, Plugin plugin) {
        this.javalin = javalin;
        configuration = new Configuration(plugin);
    }

    public static Api create(Plugin plugin) {
        var classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PluginJam.class.getClassLoader());
        var javalin = Javalin.create();
        javalin.start("0.0.0.0", Integer.parseInt(System.getProperty("javalin.port", "30000")));
        Thread.currentThread().setContextClassLoader(classLoader);
        var api = new Api(javalin, plugin);
        api.ignite();
        return api;
    }

    private void ignite() {
        javalin.routes(() -> {
            before(ctx -> log.debug("Received request on {}.", ctx.path()));
            path("v1", configuration::buildRoutes);
        });
    }
}
