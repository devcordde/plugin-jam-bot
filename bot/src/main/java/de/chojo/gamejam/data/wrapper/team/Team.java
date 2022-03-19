/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

public class Team {
    private final int id;
    private final String name;
    private final long leader;
    private final long roleId;
    private final long textChannelId;
    private final long voiceChannelId;

    public Team(int id, String name, long leader, long roleId, long textChannelId, long voiceChannelId) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.roleId = roleId;
        this.textChannelId = textChannelId;
        this.voiceChannelId = voiceChannelId;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long leader() {
        return leader;
    }

    public long roleId() {
        return roleId;
    }

    public long textChannelId() {
        return textChannelId;
    }

    public long voiceChannelId() {
        return voiceChannelId;
    }
}
