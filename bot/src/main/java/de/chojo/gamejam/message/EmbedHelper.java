/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.message;

import de.chojo.gamejam.server.TeamServer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.CompletableFuture;

public class EmbedHelper {

    public static CompletableFuture<MessageEmbed> embedDetailedStatus(TeamServer teamServer, EventContext context) {
        return CompletableFuture.supplyAsync(() -> {

            var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle("%s #%d | %s".formatted(teamServer.statusEmoji(), teamServer.team().id(), teamServer.team().meta().name()));
            if (!teamServer.exists()) {
                builder.setDescription("teamserver.message.detailstatus.nonexisting.description");
            } else {
                if (teamServer.isRunning()) {
                    builder.setDescription("teamserver.message.detailstatus.existing.description");

                    var serverStats = teamServer.stats();
                    serverStats.ifPresent(stats -> {
                        var memory = stats.memory();
                        builder.addField("word.memory", "$word.used$ %d%n$word.total$: %d%n$word.max$: %d".formatted(memory.usedMb(), memory.totalMb(), memory.maxMb()), true)
                                .addField("word.tps", "1 $word.min$: %.2f%n5 $word.min$: %.2f%n 15 $word.min$: %.2f%n$word.averageticktime$ %.2f".formatted(
                                        stats.tps()[0], stats.tps()[1], stats.tps()[2], stats.averageTickTime()), true)
                                .addField("word.players", String.valueOf(stats.onlinePlayers()), true)
                                .addField("word.system", "$word.activethreads$: %d".formatted(stats.activeThreads()), true);
                    });
                } else {
                    builder.setDescription("word.serversetup")
                            .addField("word.ports", "word.notrunning", true);
                }
            }

            return builder.build();
        });
    }
}
