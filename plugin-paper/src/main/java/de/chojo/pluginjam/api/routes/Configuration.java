/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.api.routes;

import io.javalin.http.HttpCode;
import org.bukkit.plugin.Plugin;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Configuration {
    private final Plugin plugin;

    public Configuration(Plugin plugin) {
        this.plugin = plugin;
    }

    public void buildRoutes() {
        path("config", () -> {
            post("message", ctx -> {
                plugin.getConfig().set("message", ctx.body());
                plugin.saveConfig();
                ctx.status(HttpCode.OK);
            });

            post("maxplayers", ctx -> {
                plugin.getConfig().set("maxplayers", Integer.parseInt(ctx.body()));
                plugin.saveConfig();
                ctx.status(HttpCode.OK);
            });

            post("spectatoroverflow", ctx -> {
                plugin.getConfig().set("spectatoroverflow", Boolean.parseBoolean(ctx.body()));
                plugin.saveConfig();
                ctx.status(HttpCode.OK);
            });

            post("reviewmode", ctx -> {
                plugin.getConfig().set("spectatoroverflow", Boolean.parseBoolean(ctx.body()));
                plugin.saveConfig();
                ctx.status(HttpCode.OK);
            });

            post("whitelist", ctx -> {
                plugin.getServer().setWhitelist(Boolean.parseBoolean(ctx.body()));
                ctx.status(HttpCode.OK);
            });
        });
    }
}
