/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.wrapper.team.JamTeam;

import java.util.HashMap;
import java.util.Map;

public class ServerService {
    private final Map<JamTeam, TeamServer> server = new HashMap<>();
    private final Configuration configuration;
    private int lastPort;

    public ServerService(Configuration configuration) {
        this.configuration = configuration;
        lastPort = configuration.serverManagement().minPort();
    }

    public TeamServer create(JamTeam team) {
        return server.computeIfAbsent(team, key -> new TeamServer(this, key, configuration, nextPort(), nextPort()));
    }

    private int nextPort() {
        if (lastPort >= configuration.serverManagement().maxPort()) throw new RuntimeException("Ports exhausted");
        return lastPort++;
    }
}
