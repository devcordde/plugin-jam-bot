/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.wrapper.team.JamTeam;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ServerService {
    private final Map<JamTeam, TeamServer> server = new HashMap<>();
    private final Configuration configuration;
    private int lastPort;
    private final Queue<Integer> freePorts = new ArrayDeque<>();

    public ServerService(Configuration configuration) {
        this.configuration = configuration;
        lastPort = configuration.serverManagement().minPort();
    }

    public TeamServer create(JamTeam team) {
        return server.computeIfAbsent(team, key -> new TeamServer(this, key, configuration, nextPort(), nextPort()));
    }

    private int nextPort() {
        if (!freePorts.isEmpty()) {
            return freePorts.poll();
        }

        if (lastPort >= configuration.serverManagement().maxPort()) throw new RuntimeException("Ports exhausted");
        return lastPort++;
    }

    void stopped(TeamServer server, boolean restart) {
        this.server.remove(server.team());
        freePorts.add(server.port());
        freePorts.add(server.apiPort());
        if (restart) {
            create(server.team()).start();
        }
    }
}
