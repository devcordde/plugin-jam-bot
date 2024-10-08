/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api;

import de.chojo.gamejam.api.exception.InterruptException;
import de.chojo.gamejam.api.v1.Server;
import de.chojo.gamejam.api.v1.Teams;
import de.chojo.gamejam.api.v1.Users;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
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
    private final Guilds guilds;
    private final de.chojo.gamejam.data.access.Teams teams;
    private final ServerService serverService;
    private Javalin app;

    public static Api create(Configuration configuration, ShardManager shardManager, Guilds guilds, de.chojo.gamejam.data.access.Teams teams, ServerService serverService) {
        var api = new Api(configuration, shardManager, guilds, teams, serverService);
        api.build();
        return api;
    }

    public Api(Configuration configuration, ShardManager shardManager, Guilds guilds, de.chojo.gamejam.data.access.Teams teams, ServerService serverService) {
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.guilds = guilds;
        this.teams = teams;
        this.serverService = serverService;
    }

    private void build() {
        app = Javalin.create(config -> {
            config.registerPlugin(getConfiguredOpenApiPlugin());
            config.accessManager((handler, ctx, routeRoles) -> {
                if(ctx.path().startsWith("/swagger") || ctx.path().startsWith("/redoc") || ctx.path().startsWith("/api/v1/server/plugin")){
                    handler.handle(ctx);
                    return;
                }

                var token = ctx.req.getHeader("authorization");
                if (token == null) {
                    ctx.status(HttpServletResponse.SC_UNAUTHORIZED).result("Please provide a valid token in the authorization header.");
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
                var users = new Users(shardManager, guilds);
                users.routes();
                var teams = new Teams(shardManager, guilds);
                teams.routes();
                Server server = new Server(serverService, this.teams);
                server.routes();
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

    private static OpenApiPlugin getConfiguredOpenApiPlugin() {
        var info = new io.swagger.v3.oas.models.info.Info().version("1.0").description("User API");
        OpenApiOptions options = new OpenApiOptions(info)
                .activateAnnotationScanningFor("io.javalin.example.java")
                .path("/swagger-docs") // endpoint for OpenAPI json
                .swagger(new SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .defaultDocumentation(doc -> {
                    doc.header("authorization", String.class)
                    .result("401");
                });
        return new OpenApiPlugin(options);
    }

    public void shutdown() {
        app.stop();
    }
}
