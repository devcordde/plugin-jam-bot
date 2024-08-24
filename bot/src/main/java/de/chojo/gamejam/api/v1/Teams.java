/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1;

import de.chojo.gamejam.api.exception.Interrupt;
import de.chojo.gamejam.api.exception.InterruptException;
import de.chojo.gamejam.api.v1.wrapper.LeaderToken;
import de.chojo.gamejam.api.v1.wrapper.TeamProfile;
import de.chojo.gamejam.api.v1.wrapper.UserProfile;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
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
    private final Guilds guilds;

    public Teams(ShardManager shardManager, Guilds guilds) {
        this.shardManager = shardManager;
        this.guilds = guilds;
    }

    /*
    OpenApiBuilder.documented(OpenApiBuilder.document()
                            .header("leader-authorization", String.class)
                            .operation(operation -> {
                                operation.summary("Changes the name of the team.");
                            }).json("202", TeamProfile.class)
                            .result("403")
                            .result("409"),
     */
    /*
    OpenApiBuilder.documented(OpenApiBuilder.document()
                            .header("leader-authorization", String.class)
                            .operation(operation -> {
                                operation.summary("Changes the github link of the team.");
                            }).json("202", TeamProfile.class)
                            .result("403")
     */
    private static void putName(Context ctx) {
// change name
    }

    private static void putGithub(Context ctx) {
// change github link
    }

    /*
    OpenApiBuilder.documented(OpenApiBuilder.document()
                            .header("leader-authorization", String.class)
                            .operation(operation -> {
                                operation.summary("Changes the leader of the team.");
                            }).json("202", TeamProfile.class)
                            .result("403")
     */
    private static void putLeader(Context ctx) {
// change leader
    }

    /*
    OpenApiBuilder.documented(OpenApiBuilder.document()
                            .header("leader-authorization", String.class)
                            .operation(operation -> {
                                operation.summary("Changes the description of the team.");
                            }).json("202", TeamProfile.class)
                            .result("403")
     */
    private static void putDescription(Context ctx) {
// change description
    }

    /*
     OpenApiBuilder.documented(OpenApiBuilder.document()
                            .header("leader-authorization", String.class)
                            .operation(operation -> {
                                operation.summary("Checks if the provided authorization header is still valid");
                            }).json("200", LeaderToken.class)
                            .result("403")
     */
    private void authCheck(Context ctx) throws InterruptException {
        String token = ctx.header("leader-authorization");
        if (token == null) {
            throw Interrupt.create("Set the \"leader-authorization\" header to get access.", HttpStatus.UNAUTHORIZED);
        }

        Interrupt.assertForbidden("123456".equals(token));
        new LeaderToken(0, token, true);
        ctx.status(HttpStatus.OK).result("Valid");
// returns ok if token is valid
    }

    // OpenApi.documented(OpenApiBuilder.document()
    //                                    .operation(operation -> {
    //                                        operation.summary("Get the leader token when the user id matched the leader id")
    //                                                .description("""
    //                                                        The returned token stays valid until the leader of the team changes.
    //                                                        A new request will always yield the same token as long as the leader stays.
    //                                                        The token has to be send via the "leader-authorization" header.""".stripIndent());
    //                                    })
    //                                    .json("200", LeaderToken.class)
    //                                    .result("403")
    private void authorize(Context ctx) {
        ctx.status(HttpStatus.OK).result("123456");
// returns a modification token or forbidden if id is not leader
    }

    public void routes() {
        path("teams", () -> {
            get("{guild-id}", this::getGuildTeams);

            path("{guild-id}/{team-id}", () -> {
                get("member", this::getGuildTeamMembers);
                get("profile", this::getTeamProfile);

                path("leader", () -> {
                    get("auth/{user-id}", this::authorize);

                    get("auth/check", this::authCheck);

                    put("name", Teams::putName);

                    put("projecturl", Teams::putGithub);

                    put("leader", Teams::putLeader);

                    put("description", Teams::putDescription);
                });
            });
        });
    }

    private LeaderPath resolveTeamPath(Context ctx) throws InterruptException {
        var guild = shardManager.getGuildById(ctx.pathParamAsClass("guild-id", Long.class).get());
        Interrupt.assertNotFound(guild, "Guild");
        var team = guilds.guild(guild).teams().byId(ctx.pathParamAsClass("team-id", Integer.class).get());
        Interrupt.assertNotFound(team.isEmpty(), "Team");
        return new LeaderPath(team.get(), guild);
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

    @OpenApi(path = "/api/v1/teams/{guild-id}",
            description = "Get all teams on a guild",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = TeamProfile[].class))
            },
            pathParams = {
                    @OpenApiParam(
                            name = "guild-id",
                            description = "Id of the guild",
                            required = true)})
    private void getGuildTeams(Context ctx) throws InterruptException {
        var guild = resolveGuild(ctx);
        var jamGuild = guilds.guild(guild);
        var jam = jamGuild.jams().nextOrCurrent();
        Interrupt.assertNoJam(jam.isEmpty());
        ctx.status(HttpStatus.OK).json(jam.get().teams().teams().stream().map(TeamProfile::build).toList());
    }

    @OpenApi(path = "/api/v1/teams/{guild-id}/{team-id}/member",
            description = "Get the member list of a team on a guild",
            methods = {HttpMethod.GET},
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = UserProfile[].class)),
            },
            pathParams = {
                    @OpenApiParam(
                            name = "guild-id",
                            description = "Id of the guild",
                            required = true),
                    @OpenApiParam(name = "team-id",
                            description = "Id of the team",
                            required = true)})
    private void getGuildTeamMembers(Context ctx) throws InterruptException {
        var teamPath = resolveTeamPath(ctx);
        var teamMember = teamPath.team.member();
        List<Member> members = new ArrayList<>();
        for (var member : teamMember) {
            members.add(getMember(teamPath.guild(), member.member().getIdLong()));
        }
        ctx.status(HttpStatus.OK).json(members.stream().map(UserProfile::build).toList());
    }

    @OpenApi(path = "/api/v1/teams/{guild-id}/{team-id}/profile",
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = TeamProfile.class)),

            },
            pathParams = {
                    @OpenApiParam(
                            name = "guild-id",
                            description = "Id of the guild",
                            required = true),
                    @OpenApiParam(name = "team-id",
                            description = "Id of the team",
                            required = true)})
    private void getTeamProfile(Context ctx) throws InterruptException {
        var teamPath = resolveTeamPath(ctx);
        ctx.status(HttpStatus.OK).json(TeamProfile.build(teamPath.team()));
    }

    private record LeaderPath(Team team, Guild guild) {
    }
}
