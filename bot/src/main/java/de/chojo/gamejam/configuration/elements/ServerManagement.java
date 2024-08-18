/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ServerManagement {
    private String serverDir= "server";
    private int minPort = 30001;
    private int maxPort = 30500;
    private String velocityHost = "velocity";
    private int velocityPort = 30000;
    private int maxPlayers = 50;
    private int memory = 1024;

    private List<String> parameter = new ArrayList<>();

    public int minPort() {
        return minPort;
    }

    public int maxPort() {
        return maxPort;
    }

    public int velocityPort() {
        return velocityPort;
    }

    public List<String> parameter() {
        return parameter;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public int memory() {
        return memory;
    }

    public String serverDir() {
        return serverDir;
    }

    public String getVelocityHost() {
        return velocityHost;
    }
}
