/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.team;

import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.localization.ContextLocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
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

    public boolean matchName(String name) {
        if (name.isBlank()) return true;
        return name.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT));
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JamTeam jamTeam)) return false;

        return id == jamTeam.id;
    }

    @Override
    public int hashCode() {
        return id;
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

    public void leave(SlashCommandInteractionEvent event, SlashCommandContext context, TeamData teamData) {
        var guild = event.getGuild();
        var member = event.getMember();
        guild.removeRoleFromMember(member, guild.getRoleById(roleId())).queue();
        guild.getTextChannelById(textChannelId()).sendMessage(context.localize("command.team.leave.leftBroadcast", Replacement.createMention(member))).queue();
        event.reply(context.localize("command.team.leave.left")).setEphemeral(true).queue();
        teamData.leaveTeam(this, event.getMember());
    }
}
