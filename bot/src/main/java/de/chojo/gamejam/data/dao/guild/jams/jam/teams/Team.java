/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.teams;

import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.team.TeamMember;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.team.TeamMeta;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.team.TeamVote;
import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Team extends QueryFactory {
    private final Jam jam;
    private final int id;
    private TeamMeta meta;

    public Team(Jam jam, int id) {
        super(jam);
        this.jam = jam;
        this.id = id;
    }

    public void delete() {
        var meta = meta();
        meta.textChannel().ifPresent(channel -> channel.delete().queue());
        meta.voiceChannel().ifPresent(channel -> channel.delete().queue());
        meta.role().ifPresent(role -> role.delete().queue());
    }

    public MessageEmbed profileEmbed(LocalizationContext localizer) {

        var member = member().stream()
                             .map(u -> u.member().getAsMention())
                             .collect(Collectors.joining(", "));

        var meta = meta();
        return new LocalizedEmbedBuilder(localizer)
                .setTitle(meta.name())
                .setDescription(meta.projectDescription())
                .addField("command.team.profile.member", member, true)
                .addField("command.team.profile.leader", MentionUtil.user(meta.leader()), true)
                .addField("command.team.profile.projecturl", meta.projectUrl(), true)
                .setFooter(String.format("#%s", id()))
                .build();
    }

    public List<TeamMember> member() {
        return builder(TeamMember.class)
                .query("SELECT user_id FROM team_member WHERE team_id = ?")
                .parameter(p -> p.setInt(id()))
                .readRow(r -> {
                    try {
                        var member = jam.jamGuild().guild().retrieveMemberById(r.getLong("user_id")).complete();
                        return new TeamMember(this, member);
                    } catch (RuntimeException e) {
                        return null;
                    }
                })
                .allSync()
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }

    public int id() {
        return id;
    }

    public boolean matchName(String name) {
        if (name.isBlank()) return true;
        return meta().name().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT));
    }

    public boolean isLeader(ISnowflake snowflake) {
        return meta().leader() == snowflake.getIdLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team team)) return false;

        return id == team.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public List<TeamVote> votes() {
        return builder(TeamVote.class)
                .query("""
                       SELECT
                           rank, team_id, points, jam_id
                       FROM team_ranking
                       WHERE team_id = ?
                       """)
                .parameter(p -> p.setInt(id()))
                .readRow(r -> new TeamVote(this, r.getInt("rank"), r.getInt("points")))
                .allSync();
    }

    public boolean vote(Member member, int points) {
        return builder()
                .query("""
                       INSERT INTO vote(team_id, voter_id, points) VALUES (?,?,?)
                       ON CONFLICT (team_id, voter_id)
                           DO UPDATE SET points = excluded.points;
                       """)
                .parameter(p -> p.setInt(id()).setLong(member.getIdLong()).setInt(points))
                .insert()
                .sendSync()
                .changed();
    }


    public Jam jam() {
        return jam;
    }

    public boolean disband() {
        delete();
        return builder()
                .query("DELETE FROM team WHERE id = ?")
                .parameter(p -> p.setInt(id()))
                .insert()
                .sendSync()
                .changed();
    }

    public TeamMeta meta() {
        if (meta == null) {
            meta = builder(TeamMeta.class)
                    .query("""
                           SELECT team_name,
                                  leader_id,
                                  role_id,
                                  text_channel_id,
                                  voice_channel_id,
                                  project_description,
                                  project_url
                           FROM team_meta WHERE team_id = ?
                           """)
                    .parameter(stmt -> stmt.setInt(id))
                    .readRow(row -> new TeamMeta(this,
                            row.getString("team_name"),
                            row.getLong("leader_id"),
                            row.getLong("role_id"),
                            row.getLong("text_channel_id"),
                            row.getLong("voice_channel_id"),
                            row.getString("project_description"),
                            row.getString("project_url")))
                    .firstSync()
                    .orElseThrow();
        }
        return meta;
    }

    public Optional<TeamMember> member(Member member) {
        return builder(TeamMember.class)
                .query("SELECT user_id FROM team_member WHERE team_id = ? AND user_id = ?")
                .parameter(p -> p.setInt(id()).setLong(member.getIdLong()))
                .readRow(r -> new TeamMember(this, member))
                .firstSync();
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(meta().name(), id);
    }

    public Integer votes(Member member) {
        return builder(Integer.class)
                .query("SELECT sum(points) as points FROM vote WHERE team_id = ? AND voter_id = ?")
                .parameter(stmt -> stmt.setInt(id()).setLong(member.getIdLong()))
                .readRow(r -> r.getInt("points"))
                .firstSync()
                .orElse(0);
    }
}
