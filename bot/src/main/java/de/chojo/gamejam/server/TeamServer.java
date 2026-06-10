/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamServer {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSSS");
    private static final Logger log = getLogger(TeamServer.class);
    private final Team team;
    private final String containerName;

    public TeamServer(Team team, String containerName) {
        this.team = team;
        this.containerName = containerName;
    }

    private String teamName() {
        return team.meta().name().toLowerCase().replace(" ", "_");
    }

    public Team team() {
        return team;
    }

    @Override
    public String toString() {
        return "TeamServer{" +
                "team=" + team + "," +
                "containerName='" + containerName +
                '}';
    }

    public String containerName() {
        return containerName;
    }
}
