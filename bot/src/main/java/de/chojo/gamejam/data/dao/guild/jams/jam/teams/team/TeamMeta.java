/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.teams.team;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;

public class TeamMeta extends QueryFactory {
    private final Team team;
    private String name;
    private long leader;
    private long role;
    private long textChannel;
    private long voiceChannel;


    public TeamMeta(Team team, String name, long leader, long role, long textChannel, long voiceChannel) {
        super(team);
        this.team = team;
        this.name = name;
        this.leader = leader;
        this.role = role;
        this.textChannel = textChannel;
        this.voiceChannel = voiceChannel;
    }

    public long leader() {
        return leader;
    }

    public void leader(long leader) {
        if (set("leader_id", leader)) {
            this.leader = leader;
        }
    }

    public long role() {
        return role;
    }

    public long textChannel() {
        return textChannel;
    }

    public long voiceChannel() {
        return voiceChannel;
    }

    public void role(long role) {
        if (set("role_id", role)) {
            this.role = role;
        }
    }

    public void textChannel(long textChannel) {
        if (set("text_channel_id", textChannel)) {
            this.textChannel = textChannel;
        }
    }

    public void voiceChannel(long voiceChannel) {
        if (set("voice_channel_id", voiceChannel)) {
            this.voiceChannel = voiceChannel;
        }
    }

    public String name() {
        return name;
    }

    public void rename(String name) {
        var changed = builder()
                .query("""
                       UPDATE team_meta SET name = ? WHERE team_id = ?
                       """)
                .parameter(stmt -> stmt.setString(name).setInt(team.id()))
                .update()
                .sendSync()
                .changed();
        if (changed) {
            var guild = team.jam().jamGuild().guild();
            this.name = name;
            guild.getRoleById(role).getManager().setName(name()).queue();
            guild.getTextChannelById(textChannel).getManager().setName(name().replace(" ", "-")).queue();
            guild.getVoiceChannelById(voiceChannel).getManager().setName(name()).queue();
        }
    }

    private boolean set(String column, long value) {
        return builder()
                .query("""
                       INSERT INTO team_meta(team_id, %s) VALUES(?,?)
                       ON CONFLICT(team_id)
                           DO UPDATE
                               SET %s = excluded.%s
                       """, column, column, column)
                .parameter(p -> p.setInt(team.id()).setLong(value))
                .update()
                .sendSync()
                .changed();
    }
}
