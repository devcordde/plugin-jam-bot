/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public record JamTeam(int id, String name, long leader, long roleId, long textChannelId,
                      long voiceChannelId) {

    public static JamTeam create(String name, Member leader, Role role, TextChannel textChannel, VoiceChannel voiceChannel) {
        return new JamTeam(-1,
                name,
                leader.getIdLong(),
                role.getIdLong(),
                textChannel.getIdLong(),
                voiceChannel.getIdLong());
    }

    public void delete(Guild guild) {
        guild.getTextChannelById(textChannelId()).delete().queue();
        guild.getVoiceChannelById(voiceChannelId()).delete().queue();
        guild.getRoleById(roleId()).delete().queue();
    }
}
