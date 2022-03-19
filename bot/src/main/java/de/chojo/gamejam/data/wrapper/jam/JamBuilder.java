/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import de.chojo.gamejam.data.wrapper.team.Team;

import java.util.List;

public class JamBuilder {
    private final int id;
    private final boolean active;
    private JamTimes times;
    private String topic;
    private List<Long> registrations;
    private List<Team> teams;

    public JamBuilder(int id, boolean active) {
        this.id = id;
        this.active = active;
    }

    public JamBuilder setTimes(JamTimes times) {
        this.times = times;
        return this;
    }

    public JamBuilder setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public JamBuilder setRegistrations(List<Long> registrations) {
        this.registrations = registrations;
        return this;
    }

    public JamBuilder setTeams(List<Team> teams) {
        this.teams = teams;
        return this;
    }

    public Jam build() {
        return new Jam(id, active, times, topic, registrations, teams);
    }

    public int id() {
        return id;
    }
}
