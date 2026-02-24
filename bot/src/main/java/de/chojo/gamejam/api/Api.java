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
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.util.Set;
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
    private final Set<String> unauthorized = Set.of(
            "/api/swagger",
            "/api/openapi.json",
            "/api/v1/server/plugin",
            "/webjars");

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
            config.concurrency.useVirtualThreads = true;
            config.requestLogger.http((ctx, executionTimeMs) -> {
                log.debug("{}: {} in {}ms\nHeaders:\n{}\nBody:\n{}",
                        ctx.method(), ctx.path(), executionTimeMs,
                        headers(ctx),
                        ctx.body().substring(0, Math.min(100, ctx.body().length())));
            });
            config.routes.apiBuilder(this::routes);
            config.registerPlugin(openApi());
            config.registerPlugin(swagger());


            config.routes.exception(InterruptException.class, (exception, ctx) -> {
                ctx.status(exception.status()).result(exception.getMessage());
            });

            config.routes.beforeMatched(ctx -> {
                for (String path : unauthorized) {
                    if(ctx.path().startsWith(path)) return;
                }

                var token = ctx.req().getHeader("authorization");
                if (token == null) {
                    throw new UnauthorizedResponse();
                } else if (!token.equals(configuration.api().token())) {
                    throw new UnauthorizedResponse();
                }
            });

        }).start(configuration.api().host(), configuration.api().port());
    }

    private void routes() {
        path("api/v1", () -> {
            var users = new Users(shardManager, guilds);
            users.routes();
            var teams = new Teams(shardManager, guilds);
            teams.routes();
            Server server = new Server(serverService, this.teams);
            server.routes();
        });

    }

    private String headers(Context context) {
        return context.headerMap()
                .entrySet()
                .stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n  "));
    }

    private OpenApiPlugin openApi() {
        return new OpenApiPlugin(config ->
                config.withDocumentationPath("/api/openapi.json")
                        .withDefinitionConfiguration((version, definition) ->
                                definition.info(info ->
                                                info.description("Plugin Jam Bot")
                                                        .license("AGPL-3.0")
                                        )
                                        .server(server ->
                                                server.description("Lyna Backend")
                                                        .url(configuration.api().url()))
                                        .withApiKeyAuth("Authorization", "Authorization")
                        )
        );
    }

    private SwaggerPlugin swagger() {
        return new SwaggerPlugin(conf -> {
            conf.withUiPath("/api/swagger");
            conf.withDocumentationPath("/api/openapi.json");
        });
    }

    public void shutdown() {
        app.stop();
    }
}
