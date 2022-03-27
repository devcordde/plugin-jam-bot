/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.localization.ContextLocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;

public final class JamTeam {
    private final int id;
    private String name;
    private long leader;
    private final long roleId;
    private final long textChannelId;
    private final long voiceChannelId;

    public JamTeam(int id, String name, long leader, long roleId, long textChannelId,
                   long voiceChannelId) {
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

    public void init(Guild guild, Category category) {
        var role = guild
                .createRole()
                .setPermissions(0L)
                .setMentionable(false)
                .setHoisted(false)
                .setName(name())
                .complete();

        var text = guild.createTextChannel(name().replace(" ", "-"), category)
                .addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet())
                .addMemberPermissionOverride(guild.getJDA().getSelfUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptySet())
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();

        var voice = guild.createVoiceChannel(name(), category)
                .addRolePermissionOverride(role.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), Collections.emptySet())
                .addMemberPermissionOverride(guild.getJDA().getSelfUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptySet())
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), Collections.emptySet(), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();
    }

    public void rename(Guild guild, String name) {
        this.name = name;
        guild.getRoleById(roleId).getManager().setName(name()).queue();
        guild.getTextChannelById(textChannelId).getManager().setName(name().replace(" ", "-")).queue();
        guild.getVoiceChannelById(voiceChannelId).getManager().setName(name()).queue();
    }

    public void delete(Guild guild) {
        guild.getTextChannelById(textChannelId()).delete().queue();
        guild.getVoiceChannelById(voiceChannelId()).delete().queue();
        guild.getRoleById(roleId()).delete().queue();
    }

    public MessageEmbed profileEmbed(TeamData teamData, ContextLocalizer localizer) {
        var member = teamData.getMember(this).join().stream()
                .map(u -> MentionUtil.user(u.userId()))
                .collect(Collectors.joining(", "));

        return new LocalizedEmbedBuilder(localizer)
                .setTitle(name())
                .addField("command.team.profile.member", member, true)
                .addField("command.team.profile.leader", MentionUtil.user(leader()), true)
                .setFooter(String.format("#%s", id()))
                .build();
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JamTeam) obj;
        return this.id == that.id &&
               Objects.equals(this.name, that.name) &&
               this.leader == that.leader &&
               this.roleId == that.roleId &&
               this.textChannelId == that.textChannelId &&
               this.voiceChannelId == that.voiceChannelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, leader, roleId, textChannelId, voiceChannelId);
    }

    @Override
    public String toString() {
        return "JamTeam[" +
               "id=" + id + ", " +
               "name=" + name + ", " +
               "leader=" + leader + ", " +
               "roleId=" + roleId + ", " +
               "textChannelId=" + textChannelId + ", " +
               "voiceChannelId=" + voiceChannelId + ']';
    }

    public void leader(long leader) {
        this.leader = leader;
    }
}
