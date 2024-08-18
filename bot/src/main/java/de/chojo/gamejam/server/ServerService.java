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
import java.util.Stack;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerService implements Runnable {
    private static final Logger log = getLogger(ServerService.class);
    private final Map<Team, TeamServer> server = new HashMap<>();
    private Teams teams;
    private final Configuration configuration;
    private final Stack<Integer> freePorts = new Stack<>();

    public static ServerService create(ScheduledExecutorService executorService, Configuration configuration) {
        var serverService = new ServerService(configuration);
        executorService.scheduleAtFixedRate(serverService, 10, 10, TimeUnit.SECONDS);
        return serverService;
    }

    private ServerService(Configuration configuration) {
        this.configuration = configuration;
        IntStream.rangeClosed(configuration.serverManagement().minPort(), configuration.serverManagement().maxPort())
                .forEach(freePorts::add);
    }

    @Override
    public void run() {
        for (var value : server.values()) {
            if (!value.running()) continue;
            try {
                value.serverRequests()
                        .ifPresent(server -> {
                            if (server.restart()) {
                                log.info("Server of team {} requested restart", value.team());
                                value.restart();
                            }
                        });
            } catch (RuntimeException e) {
                log.error("Could not reach server {}", value);
            }
        }
    }

    public void syncVelocity() {
        log.info("Syncing server with velocity instance.");
        freePorts.clear();
        IntStream.rangeClosed(configuration.serverManagement().minPort(), configuration.serverManagement().maxPort())
                .forEach(freePorts::add);
        var velocityPort = configuration.serverManagement().velocityPort();
        var velocityHost = configuration.serverManagement().getVelocityHost();
        var httpClient = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create("http://%s:%d/v1/server".formatted(velocityHost, velocityPort)))
                .GET()
                .build();
        HttpResponse<String> response;
        while (true) {
            try {
                response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                break;
            } catch (IOException e) {
                log.error("Could not reach velocity instance", e);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        var collectionType = Mapper.MAPPER.getTypeFactory()
                .constructCollectionType(List.class, Registration.class);
        List<Registration> registrations;
        try {
            registrations = Mapper.MAPPER.readValue(response.body(), collectionType);
        } catch (JsonProcessingException e) {
            log.error("Could not map response");
            throw new RuntimeException(e);
        }

        server.clear();
        for (var registration : registrations) {
            var optTeam = teams.byId(registration.id());
            if (optTeam.isEmpty()) {
                log.warn("Could not find a matching team for id {} of team {}", registration.id(), registration.name());
                return;
            }
            var team = optTeam.get();
            log.info("Registered server for team {} with id {}", team.meta().name(), team.id());
            var teamServer = new TeamServer(this, team, configuration, registration.port(), registration.apiPort());
            teamServer.running(true);
            server.put(team, teamServer);
            freePorts.removeElement(registration.apiPort());
            freePorts.removeElement(registration.port());
        }
    }

    public TeamServer get(Team team) {
        return server.computeIfAbsent(team, key -> new TeamServer(this, key, configuration, nextPort(), nextPort()));
    }

    private int nextPort() {
        if (!freePorts.isEmpty()) {
            return freePorts.pop();
        }
        throw new RuntimeException("Ports exhausted");
    }

    void stopped(TeamServer server, boolean restart) {
        this.server.remove(server.team());
        freePorts.push(server.port());
        freePorts.push(server.apiPort());
        if (restart) {
            get(server.team()).start();
        }
    }

    public void inject(Teams teams) {
        this.teams = teams;
    }
}
