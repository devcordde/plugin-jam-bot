/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ServerManagement {
    private int minPort;
    private int maxPort;
    private int velocityApi;
    private int maxPlayers;
    private String template;
    private int memory;

    private List<String> parameter;

    public String template() {
        return template;
    }

    public int minPort() {
        return minPort;
    }

    public int maxPort() {
        return maxPort;
    }

    public int velocityApi() {
        return velocityApi;
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
}
