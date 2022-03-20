/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class JamTeam {
    private final int id;
    private final String name;
    private final long leader;
    private final long roleId;
    private final long textChannelId;
    private final long voiceChannelId;

    public JamTeam(int id, String name, long leader, long roleId, long textChannelId, long voiceChannelId) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.roleId = roleId;
        this.textChannelId = textChannelId;
        this.voiceChannelId = voiceChannelId;
    }

    public static JamTeam create(String name, Member leader, Role role, TextChannel textChannel, VoiceChannel voiceChannel) {
        return new JamTeam(-1,
                name,
                leader.getIdLong(),
                role.getIdLong(),
                textChannel.getIdLong(),
                voiceChannel.getIdLong());
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
