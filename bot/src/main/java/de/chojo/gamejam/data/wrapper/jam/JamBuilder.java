/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import de.chojo.gamejam.data.wrapper.team.JamTeam;

import java.util.List;

public class JamBuilder {
    private final int id;
    private JamTimes times;
    private String topic;
    private List<Long> registrations;
    private List<JamTeam> teams;
    private JamState state;

    public JamBuilder(int id) {
        this.id = id;
    }

    public JamBuilder setTimes(JamTimes times) {
        this.times = times;
        return this;
    }

    public JamBuilder setState(JamState state){
        this.state = state;
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

    public JamBuilder setTeams(List<JamTeam> teams) {
        this.teams = teams;
        return this;
    }

    public Jam build() {
        return new Jam(id, times, state, topic, registrations, teams);
    }

    public int id() {
        return id;
    }
}
