/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

public class JamSettings {
    private int teamSize;
    private long jamRole;

    public JamSettings() {
        teamSize = 4;
        jamRole = 0;
    }

    public JamSettings(int teamSize, long jamRole) {
        this.teamSize = teamSize;
        this.jamRole = jamRole;
    }

    public int teamSize() {
        return teamSize;
    }

    public long jamRole() {
        return jamRole;
    }

    public void teamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public void jamRole(long jamRole) {
        this.jamRole = jamRole;
    }
}
