package de.chojo.gamejam.api.v1;

import de.chojo.gamejam.api.v1.wrapper.GuildProfile;
import de.chojo.gamejam.api.v1.wrapper.TeamProfile;
import de.chojo.gamejam.api.v1.wrapper.UserProfile;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import io.javalin.http.HttpCode;
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
                            ctx.status(HttpCode.NOT_FOUND).result("Not found");
                            return;
                        }

                        if (user == null) {
                            ctx.status(HttpCode.NOT_FOUND).result("Not found");
                            return;
                        }

                        var guildProfiles = shardManager.getMutualGuilds(user).stream().map(GuildProfile::build).toList();
                        ctx.status(HttpCode.OK).json(guildProfiles);
                    });
                });
                path("{guild-id}", () -> {
                    get("team", ctx -> {
                        var userId = ctx.pathParamAsClass("user-id", Long.class).get();
                        var guildId = ctx.pathParamAsClass("guild-id", Long.class).get();
                        var guild = shardManager.getGuildById(guildId);

                        if (guild == null) {
                            ctx.status(HttpCode.NOT_FOUND).result("Guild not found");
                            return;
                        }

                        var jam = jamData.getNextOrCurrentJam(guild).join();
                        if (jam.isEmpty()) {
                            ctx.status(HttpCode.NOT_FOUND).result("Not current jam found");
                            return;
                        }

                        Member member;
                        try {
                            member = guild.retrieveMemberById(userId).complete();
                        } catch (RuntimeException e) {
                            ctx.status(HttpCode.NOT_FOUND).result("User not found");
                            return;
                        }

                        if (member == null) {
                            ctx.status(HttpCode.NOT_FOUND).result("User not found");
                            return;
                        }

                        var team = teamData.getTeamByMember(jam.get(), member).join();
                        if (team.isEmpty()) {
                            ctx.status(HttpCode.NOT_FOUND).result("Team not found");
                            return;
                        }
                        ctx.status(HttpCode.OK).json(TeamProfile.build(team.get()));
                    });

                    get("profile", ctx -> {
                        var userId = ctx.pathParamAsClass("user-id", Long.class).get();
                        var guildId = ctx.pathParamAsClass("guild-id", Long.class).get();

                        var guild = shardManager.getGuildById(guildId);

                        if (guild == null) {
                            ctx.status(HttpCode.NOT_FOUND).result("Guild not found");
                            return;
                        }

                        Member member;
                        try {
                            member = guild.retrieveMemberById(userId).complete();
                        } catch (RuntimeException e) {
                            ctx.status(HttpCode.NOT_FOUND).result("User not found");
                            return;
                        }

                        if (member == null) {
                            ctx.status(HttpCode.NOT_FOUND).result("User not found");
                            return;
                        }
                        ctx.status(HttpCode.OK).json(UserProfile.build(member));
                    });
                });
            });
        });
    }
}
