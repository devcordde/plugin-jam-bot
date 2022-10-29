/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api.routes;

import de.chojo.pluginjam.service.ServerRequests;
import org.bukkit.plugin.Plugin;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Requests {
    private final ServerRequests requests;

    public Requests(ServerRequests requests) {
        this.requests = requests;
    }

    public void buildRoutes() {
        path("requests", () -> {
            get("", ctx -> {
                ctx.json(requests.get());
            });
        });
    }
}
