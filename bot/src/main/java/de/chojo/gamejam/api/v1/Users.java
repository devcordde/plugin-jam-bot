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
                    get(ctx -> {
                        var user = getUser(ctx.pathParamAsClass("user-id", Long.class).get());

                        var guildProfiles = shardManager.getMutualGuilds(user).stream().map(GuildProfile::build).toList();
                        ctx.status(HttpCode.OK).json(guildProfiles);
                    });
                });
                path("{guild-id}", () -> {
                    get("team", ctx -> {
                        var guildPath = resolveGuildPath(ctx);

                        var jam = jamData.getNextOrCurrentJam(guildPath.guild()).join();
                        Interrupt.assertNoJam(jam.isEmpty());

                        var team = teamData.getTeamByMember(jam.get(), guildPath.member()).join();

                        Interrupt.assertNotFound(team.isEmpty(), "Team");

                        ctx.status(HttpCode.OK).json(TeamProfile.build(team.get()));
                    });

                    get("profile", ctx -> {
                        ctx.status(HttpCode.OK).json(UserProfile.build(resolveGuildPath(ctx).member()));
                    });
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

    private record GuildPath(Member member, Guild guild) {
    }
}
