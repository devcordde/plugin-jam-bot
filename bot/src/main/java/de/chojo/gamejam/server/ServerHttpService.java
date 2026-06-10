/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.util.Mapper;
import de.chojo.pluginjam.payload.Registration;
import de.chojo.pluginjam.payload.StatsPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ServerHttpService {
    private final HttpClient http = HttpClient.newHttpClient();
    private static final int DEFAULT_API_PORT = 30000;
    private final Logger log = LoggerFactory.getLogger(ServerHttpService.class);
    private final Configuration configuration;

    public ServerHttpService(Configuration configuration) {
        this.configuration = configuration;
    }

    public Optional<StatsPayload> fetchServerStats(String containerName) {
        var request = requestBuilder("v1/stats", containerName).GET().build();

        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            var stats = Mapper.MAPPER.readValue(response.body(), StatsPayload.class);
            return Optional.of(stats);
        } catch (IOException | InterruptedException e) {
            log.error("Could not read stats", e);
        }
        return Optional.empty();
    }

    public boolean fetchOnlineStatus(String containerName) {
        var request = requestBuilder("v1/online", containerName).GET().build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            log.error("Could not read online status", e);
        }
        return false;
    }

    public boolean configureSpectatorOverflow(String containerName, boolean enabled) {
        var request = requestBuilder("v1/config/spectatoroverflow", containerName).POST(HttpRequest.BodyPublishers.ofString(Boolean.toString(enabled))).build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Could not configure spectator overflow");
            }
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            log.error("Could not configure spectator overflow", e);
            return false;
        }
    }

    public boolean configureMaxPlayers(String containerName, int maxPlayers) {
        var request = requestBuilder("v1/config/maxplayers", containerName).POST(HttpRequest.BodyPublishers.ofString(Integer.toString(maxPlayers))).build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Could not configure max players");
            }
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            log.error("Could not configure max players", e);
            return false;
        }
    }

    public boolean configureMessage(String containerName, String message) {
        var request = requestBuilder("v1/config/message", containerName).POST(HttpRequest.BodyPublishers.ofString(message)).build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Could not configure message");
            }
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            log.error("Could not configure message", e);
            return false;
        }
    }

    public void registerToVelocity(TeamServer teamServer, String containerName) {
        var registration = new Registration(teamServer.team().id(), teamServer.team().meta().name(), containerName);
        var velocityHost = configuration.serverManagement().getVelocityHost();
        var velocityPort = configuration.serverManagement().velocityPort();

        try {
            var body = Mapper.MAPPER.writeValueAsString(registration);
            var request = HttpRequest.newBuilder(URI.create("https://%s:%d/%s".formatted(velocityHost, velocityPort, "v1/server")))
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Could not register to velocity");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Could not register to velocity", e);
        }
    }

    public void unregisterFromVelocity(TeamServer teamServer, String containerName) {
        var velocityHost = configuration.serverManagement().getVelocityHost();
        var velocityPort = configuration.serverManagement().velocityPort();

        var request = HttpRequest.newBuilder(URI.create("https://%s:%d/%s?id=%d&host=%s"
                        .formatted(velocityHost, velocityPort, "v1/server", teamServer.team().id(), containerName)))
                .DELETE().build();
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Could not unregister from velocity");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Could not unregister from velocity", e);
        }
    }

    public HttpRequest.Builder requestBuilder(String path, String host) {
        return HttpRequest.newBuilder(URI.create("https://%s:%d/%s".formatted(host, DEFAULT_API_PORT, path)));
    }

    public HttpRequest.Builder requestBuilder(String path, String host, String query) {
        return HttpRequest.newBuilder(URI.create("https://%s:%d/%s?%s".formatted(host, DEFAULT_API_PORT, path, query)));
    }
}
