/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1;

import de.chojo.gamejam.api.exception.Interrupt;
import de.chojo.gamejam.api.exception.InterruptException;
import de.chojo.gamejam.api.v1.wrapper.TeamProfile;
import de.chojo.gamejam.api.v1.wrapper.UserProfile;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.data.wrapper.team.JamTeam;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.put;

public class Teams {
    private final ShardManager shardManager;
    private final TeamData teamData;
    private final JamData jamData;

    public Teams(ShardManager shardManager, TeamData teamData, JamData jamData) {
        this.shardManager = shardManager;
        this.teamData = teamData;
        this.jamData = jamData;
    }

    public void routes() {
        path("teams", () -> {
            get("{guild-id}", this::getGuildTeams);

            path("{guild-id}/{team-id}", () -> {
                get("member", this::getGuildTeamMembers);
                get("profile", this::getTeamProfile);

                path("leader", () -> {
                    get("auth/{user-id}", ctx -> {
                        ctx.status(HttpCode.OK).result("123456");
                        // returns a modification token or forbidden if id is not leader
                    });

                    get("auth/check", ctx -> {
                        ctx.status(HttpCode.OK).result("Valid");
                        // returns ok if token is valid
                    });

                    put("name", ctx -> {
                        // change name
                    });

                    put("github", ctx -> {
                        // change github link
                    });

                    put("leader", ctx -> {
                        // change leader
                    });

                    put("description", ctx -> {
                        // change description
                    });
                });
            });
        });
    }

    private TeamPath resolveTeamPath(Context ctx) throws InterruptException {
        var guild = shardManager.getGuildById(ctx.pathParamAsClass("guild-id", Long.class).get());
        Interrupt.assertNotFound(guild, "Guild");
        var team = teamData.getTeamById(ctx.pathParamAsClass("team-id", Integer.class).get()).join();
        Interrupt.assertNotFound(team.isEmpty(), "Team");
        return new TeamPath(team.get(), guild);
    }

    private Guild resolveGuild(Context ctx) throws InterruptException {
        var guild = shardManager.getGuildById(ctx.pathParamAsClass("guild-id", Long.class).get());
        Interrupt.assertNotFound(guild, "Guild");
        return guild;
    }

    @NotNull
    private Member getMember(Guild guild, long userId) throws InterruptException {
        return completeEntity(guild.retrieveMemberById(userId), "User");
    }

    private User getUser(long userId) throws InterruptException {
        return completeEntity(shardManager.retrieveUserById(userId), "User");
    }

    @NotNull
    private <T> T completeEntity(RestAction<T> action, String entity) throws InterruptException {
        try {
            var member = action.complete();
            Interrupt.assertNotFound(member, entity);
            return member;
        } catch (RuntimeException ignored) {
            throw Interrupt.notFound(entity);
        }
    }

    @OpenApi(responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = TeamProfile[].class))
    })
    private void getGuildTeams(Context ctx) throws InterruptException {
        var guild = resolveGuild(ctx);
        var jam = jamData.getNextOrCurrentJam(guild).join();
        Interrupt.assertNoJam(jam.isEmpty());
        ctx.status(HttpCode.OK).json(jam.get().teams().stream().map(TeamProfile::build).toList());
    }

    @OpenApi(responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = UserProfile[].class))
    })
    private void getGuildTeamMembers(Context ctx) throws InterruptException {
        var teamPath = resolveTeamPath(ctx);
        var teamMember = teamData.getMember(teamPath.team()).join();
        List<Member> members = new ArrayList<>();
        for (var member : teamMember) {
            members.add(getMember(teamPath.guild(), member.userId()));
        }
        ctx.status(HttpCode.OK).json(members.stream().map(UserProfile::build).toList());
    }

    @OpenApi(responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = TeamProfile.class))
    })
    private void getTeamProfile(Context ctx) throws InterruptException {
        var teamPath = resolveTeamPath(ctx);
        ctx.status(HttpCode.OK).json(TeamProfile.build(teamPath.team()));
    }

    private record TeamPath(JamTeam team, Guild guild) {
    }
}
