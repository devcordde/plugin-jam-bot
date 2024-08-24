/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1;

import de.chojo.gamejam.data.access.Teams;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.TeamServer;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static org.slf4j.LoggerFactory.getLogger;

public class Server {
    private final ServerService serverService;
    private final Teams teams;
    private static final Logger log = getLogger(Server.class);

    public Server(ServerService serverService, Teams teams) {
        this.serverService = serverService;
        this.teams = teams;
    }

    public void routes() {
        path("server", () -> {
            post("plugin/{token}", this::handle);
        });
    }

    @OpenApi(path = "/api/v1/server/plugin/{token}",
            description = "Upload a plugin for the team",
            headers = {
                    @OpenApiParam(
                            name = "Content-Type",
                            required = true,
                            example = "application/octet-stream",
                            description = "The Content-Type. Has to be application/octet-stream.")},
            methods = {HttpMethod.POST},
            pathParams = {
                    @OpenApiParam(
                            name = "token",
                            description = "The token of the team",
                            required = true)},
            queryParams = {
                    @OpenApiParam(
                            name = "restart",
                            description = "Whether the server should restart. Default false",
                            example = "true")},
            responses = {
                    @OpenApiResponse(status = "202", description = "When the plugin was uploaded"),
                    @OpenApiResponse(status = "400", description = "When the wrong Content-Type was defined"),
                    @OpenApiResponse(status = "404", description = "When the provided token is invalid"),
                    @OpenApiResponse(status = "406", description = "When the server does not exist"),
                    @OpenApiResponse(status = "406", description = "When the server does not exist"),
                    @OpenApiResponse(status = "500", description = "When the plugin was not applied successfully"),
            }
    )
    private void handle(@NotNull Context ctx) {
        String token = ctx.pathParam("token");
        Optional<Team> team = teams.byToken(token);
        if (team.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }

        if (!"application/octet-stream".equals(ctx.contentType())) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Use Content-Type: application/octet-stream");
            return;
        }

        log.info("Received plugin upload request for {}", team.get());

        TeamServer teamServer = serverService.get(team.get());

        if (!teamServer.exists()) {
            ctx.result("Server does not exist");
            ctx.status(HttpStatus.NOT_ACCEPTABLE);
            return;
        }
        var pluginFile = teamServer.plugins().resolve("plugin.jar");
        try (var in = ctx.bodyInputStream()) {
            log.info("Writing plugin to {}", pluginFile);
            Files.copy(in, pluginFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Could not write file", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        ctx.status(HttpStatus.ACCEPTED);
        String restart = ctx.queryParam("restart");
        if ("true".equals(restart) && teamServer.running()) {
            teamServer.restart();
        } else if (teamServer.running()) {
            teamServer.send("say Plugin Updated");
        }
    }
}
