/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.web.server;

import de.chojo.pluginjam.payload.Registration;
import de.chojo.pluginjam.servers.ServerRegistry;
import io.javalin.http.HttpCode;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Server {
    private final ServerRegistry registry;

    public Server(ServerRegistry registry) {
        this.registry = registry;
    }

    public void buildRoutes() {
        path("server", () -> {
            post("", ctx -> {
                registry.register(ctx.bodyAsClass(Registration.class));
                ctx.status(HttpCode.ACCEPTED);
            });

            patch("", ctx -> {
                registry.ping(ctx.bodyAsClass(Registration.class));
                ctx.status(HttpCode.ACCEPTED);
            });

            delete("", ctx -> {
                var id = ctx.queryParam("id");
                var port = ctx.queryParam("port");
                var host = ctx.queryParam("host");
                var registration = new Registration(Integer.parseInt(id), "", host, Integer.parseInt(port), 0);
                registry.unregister(registration);
                ctx.status(HttpCode.ACCEPTED);
            });

            get("", ctx -> {
                ctx.json(registry.server());
                ctx.status(HttpCode.OK);
            });
        });
    }
}
