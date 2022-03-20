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
    private final JamTimes times;
    private final JamState state;
    private final String topic;
    private final List<Long> registrations;
    private final List<JamTeam> teams;

    public Jam(int id, JamTimes times, JamState state, String topic, List<Long> registrations, List<JamTeam> teams) {
        this.id = id;
        this.times = times;
        this.state = state;
        this.topic = topic;
        this.registrations = registrations;
        this.teams = teams;
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

    public JamState state() {
        return state;
    }
}
