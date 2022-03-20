/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import de.chojo.gamejam.data.wrapper.team.JamTeam;

import java.util.List;

public class Jam {
    private final int id;
    private final boolean active;
    private final JamTimes times;
    private final String topic;
    private final List<Long> registrations;
    private final List<JamTeam> teams;

    public Jam(int id, boolean active, JamTimes times, String topic, List<Long> registrations, List<JamTeam> teams) {
        this.id = id;
        this.active = active;
        this.times = times;
        this.topic = topic;
        this.registrations = registrations;
        this.teams = teams;
    }

    public boolean isActive() {
        return active;
    }

    public int id() {
        return id;
    }

    public JamTimes times() {
        return times;
    }

    public String topic() {
        return topic;
    }

    public List<Long> registrations() {
        return registrations;
    }

    public List<JamTeam> teams() {
        return teams;
    }
}
