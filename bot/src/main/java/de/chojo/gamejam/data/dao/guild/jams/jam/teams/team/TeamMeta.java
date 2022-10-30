/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.teams.team;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.exceptions.ThrowingConsumer;
import de.chojo.sadu.wrapper.util.ParamBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.sql.SQLException;
import java.util.Optional;

public class TeamMeta extends QueryFactory {
    private final Team team;
    private String name;
    private long leader;
    private long role;
    private long textChannel;
    private long voiceChannel;
    private String projectDescription;
    private String projectUrl;


    public TeamMeta(Team team, String name, long leader, long role, long textChannel, long voiceChannel, String projectDescription, String projectUrl) {
        super(team);
        this.team = team;
        this.name = name;
        this.leader = leader;
        this.role = role;
        this.textChannel = textChannel;
        this.voiceChannel = voiceChannel;
        this.projectDescription = projectDescription;
        this.projectUrl = projectUrl;
    }

    public long leader() {
        return leader;
    }

    public void leader(Member leader) {
        if (set("leader_id", leader.getIdLong())) {
            this.leader = leader.getIdLong();
        }
    }

    public Optional<Role> role() {
        return Optional.ofNullable(guild().getRoleById(role));
    }

    public Optional<TextChannel> textChannel() {
        return Optional.ofNullable(guild().getTextChannelById(textChannel));
    }

    private Guild guild() {
        return team.jam().jamGuild().guild();
    }

    public Optional<VoiceChannel> voiceChannel() {
        return Optional.ofNullable(guild().getVoiceChannelById(voiceChannel));
    }

    public void role(Role role) {
        if (set("role_id", role.getIdLong())) {
            this.role = role.getIdLong();
        }
    }

    public void textChannel(TextChannel textChannel) {
        if (set("text_channel_id", textChannel.getIdLong())) {
            this.textChannel = textChannel.getIdLong();
        }
    }

    public void voiceChannel(VoiceChannel voiceChannel) {
        if (set("voice_channel_id", voiceChannel.getIdLong())) {
            this.voiceChannel = voiceChannel.getIdLong();
        }
    }


    public String projectDescription() {
        return projectDescription;
    }

    public void projectDescription(String projectDescription) {
        if (set("project_description", projectDescription)) {
            this.projectDescription = projectDescription;
        }
    }

    public String projectUrl() {
        return projectUrl;
    }

    public void projectUrl(String projectUrl) {
        if (set("project_url", projectUrl)) {
            this.projectUrl = projectUrl;
        }
    }

    public String name() {
        return name;
    }

    public void rename(String name) {
        var changed = builder()
                .query("""
                       UPDATE team_meta SET team_name = ? WHERE team_id = ?
                       """)
                .parameter(stmt -> stmt.setString(name).setInt(team.id()))
                .update()
                .sendSync()
                .changed();
        if (changed) {
            this.name = name;
            role().ifPresent(role -> role.getManager().setName(name()).queue());
            textChannel().ifPresent(channel -> channel.getManager().setName(name().replace(" ", "-")).queue());
            voiceChannel().ifPresent(channel -> channel.getManager().setName(name()).queue());
        }
    }

    private boolean set(String column, long value) {
        return set(column, p -> p.setLong(value));
    }

    private boolean set(String column, String value) {
        return set(column, p -> p.setString(value));
    }

    private boolean set(String column, ThrowingConsumer<ParamBuilder, SQLException> value) {
        return builder()
                .query("""
                       INSERT INTO team_meta(team_id, team_name, %s) VALUES(?,'',?)
                       ON CONFLICT(team_id)
                           DO UPDATE
                               SET %s = excluded.%s
                       """, column, column, column)
                .parameter(p -> {
                    p.setInt(team.id());
                    value.accept(p);
                })
                .update()
                .sendSync()
                .changed();
    }
}
