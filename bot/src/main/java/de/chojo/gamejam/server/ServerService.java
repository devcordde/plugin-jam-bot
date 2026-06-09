/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Teams;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.util.Mapper;
import de.chojo.pluginjam.payload.Registration;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerService {
    private static final Logger log = getLogger(ServerService.class);
    private final Map<Team, TeamServer> server = new HashMap<>();
    private Teams teams;
    private final Configuration configuration;
    private final DockerService dockerService;

    public static ServerService create(Configuration configuration) {
        return new ServerService(configuration);
    }

    private ServerService(Configuration configuration) {
        this.configuration = configuration;
        this.dockerService = new DockerService(configuration.docker(), configuration.plugins());
        this.dockerService.initDockerClient();
    }

    public void shutdown() {
        server.forEach((team, teamServer) -> {
            teamServer.stop();
        });
    }

    public CompletableFuture<Boolean> syncVelocity() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Syncing server with velocity instance.");

            var velocityPort = configuration.serverManagement().velocityPort();
            var velocityHost = configuration.serverManagement().getVelocityHost();
            var httpClient = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(URI.create("http://%s:%d/v1/server".formatted(velocityHost, velocityPort)))
                    .GET()
                    .build();
            HttpResponse<String> response = null;
            var retries = 0;
            while (retries < 5) {
                try {
                    response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                    break;
                } catch (IOException e) {
                    log.error("Could not reach velocity instance");
                } catch (InterruptedException e) {
                    log.error("Interrupted", e);
                }
                retries++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return false;
                }
            }
            if (retries == 5) return false;
            var collectionType = Mapper.MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, Registration.class);
            List<Registration> registrations;
            try {
                registrations = Mapper.MAPPER.readValue(response.body(), collectionType);
            } catch (JsonProcessingException e) {
                log.error("Could not map response");
                return false;
            }

            server.clear();
            for (var registration : registrations) {
                var optTeam = teams.byId(registration.id());
                if (optTeam.isEmpty()) {
                    log.warn("Could not find a matching team for id {} of team {}", registration.id(), registration.name());
                    continue;
                }
                var team = optTeam.get();
                log.info("Registered server for team {} with id {}", team.meta().name(), team.id());
                var teamServer = new TeamServer(dockerService, team, configuration);
                server.put(team, teamServer);
            }
            return true;
        });
    }

    public TeamServer get(Team team) {
        return server.computeIfAbsent(team, key -> new TeamServer(dockerService, key, configuration));
    }

    public void inject(Teams teams) {
        this.teams = teams;
    }
}
