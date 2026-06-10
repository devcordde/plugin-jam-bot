/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.message;

import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.ServerStatus;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.CompletableFuture;

public class EmbedHelper {

    public static CompletableFuture<MessageEmbed> embedDetailedStatus(Team team, ServerService serverService, CommandEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            var serverStatus = serverService.getServerStatus(team.id());

            var embed = event.embed("status");
            embed.title("%s #%d | %s".formatted(serverStatus.emoji(), team.id(), team.meta().name()));

            if (serverStatus == ServerStatus.VOID) {
                embed.description("teamserver.message.detailstatus.nonexisting.description");
            } else {
                if (serverStatus == ServerStatus.RUNNING) {
                    embed.description("teamserver.message.detailstatus.existing.description");

                    var serverStats = serverService.serverHttpService().fetchServerStats(serverService.dockerService().containerName(team.id()));
                    serverStats.ifPresent(stats -> {
                        var memory = stats.memory();
                        embed.fields().add("word.memory", "$word.used$ %d%n$word.total$: %d%n$word.max$: %d".formatted(memory.usedMb(), memory.totalMb(), memory.maxMb()), true);
                        embed.fields().add("word.tps", "1 $word.min$: %.2f%n5 $word.min$: %.2f%n 15 $word.min$: %.2f%n$word.averageticktime$ %.2f".formatted(
                                stats.tps()[0], stats.tps()[1], stats.tps()[2], stats.averageTickTime()), true);
                        embed.fields().add("word.players", String.valueOf(stats.onlinePlayers()), true);
                        embed.fields().add("word.system", "$word.activethreads$: %d".formatted(stats.activeThreads()), true);
                    });
                } else {
                    embed.description("word.serversetup");
                }
            }
            return embed.build();
        });
    }
}
