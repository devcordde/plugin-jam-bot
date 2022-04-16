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
import net.dv8tion.jda.api.sharding.ShardManager;

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
                        var userId = ctx.pathParamAsClass("user-id", Long.class).get();

                        User user;
                        try {
                            user = shardManager.retrieveUserById(userId).complete();
                        } catch (RuntimeException e) {
                            throw Interrupt.notFound("User");
                        }

                        if (user == null) throw Interrupt.notFound("User");

                        var guildProfiles = shardManager.getMutualGuilds(user).stream().map(GuildProfile::build).toList();
                        ctx.status(HttpCode.OK).json(guildProfiles);
                    });
                });
                path("{guild-id}", () -> {
                    get("team", ctx -> {
                        var guildPath = resolveGuildPath(ctx);

                        var jam = jamData.getNextOrCurrentJam(guildPath.guild()).join();
                        if (jam.isEmpty()) throw Interrupt.noJam();

                        var team = teamData.getTeamByMember(jam.get(), guildPath.member()).join();

                        if (team.isEmpty()) throw Interrupt.notFound("Team");
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

        if (guild == null) throw Interrupt.notFound("Guild");

        Member member;
        try {
            member = guild.retrieveMemberById(ctx.pathParamAsClass("user-id", Long.class).get()).complete();
        } catch (RuntimeException e) {
            throw Interrupt.notFound("User");
        }

        if (member == null) throw Interrupt.notFound("User");
        return new GuildPath(member, guild);
    }

    private record GuildPath(Member member, Guild guild) {
    }
}
