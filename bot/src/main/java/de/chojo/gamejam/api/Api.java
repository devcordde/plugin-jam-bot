/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api;

import de.chojo.gamejam.api.exception.InterruptException;
import de.chojo.gamejam.api.v1.Users;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.path;
import static org.slf4j.LoggerFactory.getLogger;

public class Api {
    private static final Logger log = getLogger(Api.class);
    private Configuration configuration;
    private final ShardManager shardManager;
    private final TeamData teamData;
    private final JamData jamData;
    private Javalin app;

    public static Api create(Configuration configuration, ShardManager shardManager, TeamData teamData, JamData jamData) {
        var api = new Api(configuration, shardManager, teamData, jamData);
        api.build();
        return api;
    }

    public Api(Configuration configuration, ShardManager shardManager, TeamData teamData, JamData jamData) {
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.teamData = teamData;
        this.jamData = jamData;
    }

    private void build() {
        app = Javalin.create(config -> {
            config.accessManager((handler, ctx, routeRoles) -> {
                var token = ctx.req.getHeader("authorization");
                if (token == null) {
                    ctx.status(HttpServletResponse.SC_UNAUTHORIZED).result("Please provde a valid token in the authorization header.");
                } else if (!token.equals(configuration.api().token())) {
                    ctx.status(HttpServletResponse.SC_UNAUTHORIZED).result("Unauthorized");
                } else {
                    handler.handle(ctx);
                }
            });
            config.requestLogger((ctx, executionTimeMs) -> {
                log.debug("{}: {} in {}ms\nHeaders:\n{}\nBody:\n{}",
                        ctx.method(), ctx.path(), executionTimeMs,
                        headers(ctx),
                        ctx.body().substring(0, Math.min(100, ctx.body().length())));
            });
        }).start(configuration.api().host(), configuration.api().port());

        app.exception(InterruptException.class, (exception, ctx) -> {
            ctx.status(exception.status()).result(exception.getMessage());
        });

        app.routes(() -> {
            path("api/v1", () -> {
                var users = new Users(shardManager, teamData, jamData);
                users.routes();
            });
        });
    }

    private String headers(Context context) {
        return context.headerMap()
                .entrySet()
                .stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n  "));
    }
}
