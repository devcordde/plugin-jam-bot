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
import de.chojo.gamejam.data.access.Guilds;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
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
    private final Guilds guilds;

    public Users(ShardManager shardManager, Guilds guilds) {
        this.shardManager = shardManager;
        this.guilds = guilds;
    }

    public void routes() {
        path("users", () -> {
            path("{user-id}", () -> {
                path("guilds", () ->
                        get(this::getMutualGuilds));
                path("{guild-id}", () -> {
                    get("team", this::getUserTeam);
                    get("profile", this::getUserProfile);
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

    @OpenApi(path = "/api/v1/users/{user-id}/{guild-id}/guild",
            summary = "Get the user profile of a user on a guild",
            methods = HttpMethod.GET,
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = GuildProfile[].class)})
            })
    private void getMutualGuilds(Context ctx) throws InterruptException {
        var user = getUser(ctx.pathParamAsClass("user-id", Long.class).get());

        var guildProfiles = shardManager.getMutualGuilds(user).stream().map(GuildProfile::build).toList();
        ctx.status(HttpStatus.OK).json(guildProfiles);
    }

    @OpenApi(path = "/api/v1/users/{user-id}/{guild-id}/team",
            methods = HttpMethod.GET,
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TeamProfile.class)})
            })
    private void getUserTeam(Context ctx) throws InterruptException {
        var guildPath = resolveGuildPath(ctx);
        var guild = guilds.guild(guildPath.guild());

        var jam = guild.jams().nextOrCurrent();
        Interrupt.assertNoJam(jam.isEmpty());

        var team = jam.get().teams().byMember(guildPath.member());

        Interrupt.assertNotFound(team.isEmpty(), "Team");

        ctx.status(HttpStatus.OK).json(TeamProfile.build(team.get()));
    }

    @OpenApi(path = "/api/v1/users/{user-id}/{guild-id}/profile",
            methods = HttpMethod.GET,
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = UserProfile.class))
            })
    private void getUserProfile(Context ctx) throws InterruptException {
        ctx.status(HttpStatus.OK).json(UserProfile.build(resolveGuildPath(ctx).member()));
    }

    private record GuildPath(Member member, Guild guild) {
    }
}
