/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1;

import de.chojo.gamejam.api.exception.Interrupt;
import de.chojo.gamejam.api.exception.InterruptException;
import de.chojo.gamejam.api.v1.wrapper.GuildProfile;
import de.chojo.gamejam.api.v1.wrapper.TeamProfile;
import de.chojo.gamejam.api.v1.wrapper.UserProfile;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Users {
    private final ShardManager shardManager;
    private final TeamData teamData;
    private final JamData jamData;

    public Users(ShardManager shardManager, TeamData teamData, JamData jamData) {
        this.shardManager = shardManager;
        this.teamData = teamData;
        this.jamData = jamData;
    }

    public void routes() {
        path("users", () -> {
            path("{user-id}", () -> {
                path("guilds", () -> {
                    get(OpenApiBuilder.documented(OpenApiBuilder.document()
                            .operation(operation -> {
                                operation.summary("Get the mututal guilds with the user");
                            }).json("200", GuildProfile[].class),
                            this::getMututalGuilds));
                });
                path("{guild-id}", () -> {
                    get("team", OpenApiBuilder.documented(OpenApiBuilder.document()
                            .operation(operation -> {
                                operation.summary("Get the team of a user on a guild");
                            }).json("200", TeamProfile.class),
                            this::getUserTeam));

                    get("profile", OpenApiBuilder.documented(OpenApiBuilder.document()
                            .operation(operation -> {
                                operation.summary("Get the user profile of a user on a guild");
                            }).json("200", UserProfile.class),
                            this::getUserProfile));
                });
            });
        });
    }

    private GuildPath resolveGuildPath(Context ctx) throws InterruptException {
        var guild = shardManager.getGuildById(ctx.pathParamAsClass("guild-id", Long.class).get());
        Interrupt.assertNotFound(guild, "Guild");
        var member = getMember(guild, ctx.pathParamAsClass("user-id", Long.class).get());
        return new GuildPath(member, guild);
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

    private void getMututalGuilds(Context ctx) throws InterruptException {
        var user = getUser(ctx.pathParamAsClass("user-id", Long.class).get());

        var guildProfiles = shardManager.getMutualGuilds(user).stream().map(GuildProfile::build).toList();
        ctx.status(HttpCode.OK).json(guildProfiles);
    }

    @OpenApi(path = "/api/v1/users/{user-id}/{guild-id}/team",
            method = HttpMethod.GET,
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = TeamProfile.class))
            })
    private void getUserTeam(Context ctx) throws InterruptException {
        var guildPath = resolveGuildPath(ctx);

        var jam = jamData.getNextOrCurrentJam(guildPath.guild());
        Interrupt.assertNoJam(jam.isEmpty());

        var team = teamData.getTeamByMember(jam.get(), guildPath.member());

        Interrupt.assertNotFound(team.isEmpty(), "Team");

        ctx.status(HttpCode.OK).json(TeamProfile.build(team.get()));
    }

    @OpenApi(path = "/api/v1/users/{user-id}/{guild-id}/profile",
            method = HttpMethod.GET,
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = UserProfile.class))
            })
    private void getUserProfile(Context ctx) throws InterruptException {
        ctx.status(HttpCode.OK).json(UserProfile.build(resolveGuildPath(ctx).member()));
    }

    private record GuildPath(Member member, Guild guild) {
    }
}
