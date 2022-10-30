/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam.teams.team;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Member;

public final class TeamMember extends QueryFactory {
    private final Team team;
    private final Member member;

    public TeamMember(Team team, Member member) {
        super(team);
        this.team = team;
        this.member = member;
    }

    public Team team() {
        return team;
    }

    public Member member() {
        return member;
    }

    public boolean leave() {
        var guild = team.jam().jamGuild().guild();
        var roleById = team.meta().role();

        roleById.ifPresent(role -> guild.removeRoleFromMember(member, role).queue());
        return builder()
                .query("DELETE FROM team_member WHERE team_id = ? AND user_id = ?")
                .parameter(p -> p.setInt(team.id()).setLong(member.getIdLong()))
                .insert()
                .sendSync()
                .changed();
    }
}
