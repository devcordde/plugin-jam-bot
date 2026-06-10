/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api.routes;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class OnlineRoute {

    public void buildRoutes() {
        path("online", () -> {
            get("", ctx -> {
                ctx.json("online");
            });
        });
    }
}
