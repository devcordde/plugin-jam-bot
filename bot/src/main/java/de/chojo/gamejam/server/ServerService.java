/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Teams;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerService {
    private static final Logger log = getLogger(ServerService.class);
    private final Map<Team, TeamServer> server = new HashMap<>();
    private final Configuration configuration;
    private final DockerService dockerService;
    private final ServerHttpService serverHttpService;

    public ServerService(Configuration configuration) {
        this.configuration = configuration;
        this.serverHttpService = new ServerHttpService(configuration);
        this.dockerService = new DockerService(configuration.docker(), configuration.plugins());
        this.dockerService.initDockerClient();
    }

    public void shutdown() {
        server.forEach((team, teamServer) -> {
            stopServer(teamServer);
        });
    }

    public void startServer(TeamServer teamServer) {
        var teamId = teamServer.team().id();
        var containerName = dockerService.containerName(teamId);
        dockerService.startServer(teamId);
        serverHttpService.registerToVelocity(teamServer, containerName);
    }

    public void stopServer(TeamServer teamServer) {
        var teamId = teamServer.team().id();
        var containerName = dockerService.containerName(teamId);
        serverHttpService.unregisterFromVelocity(teamServer, containerName);
        dockerService.stopServer(teamId);
    }

    public void restartServer(TeamServer teamServer) {
        var teamId = teamServer.team().id();
        var containerName = dockerService.containerName(teamId);
        serverHttpService.unregisterFromVelocity(teamServer, containerName);
        dockerService.restartServer(teamId);
        serverHttpService.registerToVelocity(teamServer, containerName);
    }

    public TeamServer get(Team team) {
        return server.computeIfAbsent(team, t -> new TeamServer(t, dockerService.containerName(t.id())));
    }

    public ServerStatus getServerStatus(int teamId) {
        if (dockerService.isRunning(teamId)) {
            return ServerStatus.VOID;
        }

        var minecraftOnline = serverHttpService.fetchOnlineStatus(dockerService.containerName(teamId));
        var containerRunning = dockerService.isRunning(teamId);

        if (minecraftOnline && containerRunning) {
            return ServerStatus.RUNNING;
        }

        if (containerRunning) {
            return ServerStatus.STARTING_STOPPING;
        }

        return ServerStatus.STOPPED;
    }

    public ServerHttpService serverHttpService() {
        return serverHttpService;
    }

    public DockerService dockerService() {
        return dockerService;
    }
}
