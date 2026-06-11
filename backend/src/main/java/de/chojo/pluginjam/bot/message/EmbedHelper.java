/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.message;

import de.chojo.pluginjam.bot.util.MentionUtil;
import de.chojo.pluginjam.database.entity.SettingsEntity;
import de.chojo.pluginjam.database.entity.jam.Jam;
import de.chojo.pluginjam.database.entity.team.Team;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class EmbedHelper {

   /* public static CompletableFuture<MessageEmbed> embedDetailedStatus(Team team, ServerService serverService, CommandEvent event) {
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

    */

    public static MessageEmbed teamProfileEmbed(Team team, CommandEvent event) {
        var embed = new EmbedBuilder();
        embed.setAuthor(team.meta().getTeamName(), null, team.meta().getProjectUrl());
        embed.setDescription(team.meta().getProjectDescription());
        team.members().forEach(member -> embed.addField(MentionUtil.user(member.userId()), "", true));
        return embed.build();
    }

    public static MessageEmbed buildJamListEmbed(List<Jam> jams, CommandEvent event, MessageResolver messageResolver, DiscordLocale locale) {
        var embed = new EmbedBuilder();
        embed.setTitle(messageResolver.resolve("word-jams", locale));
        embed.setDescription(String.join("\n", jams.stream().map(jam -> formatJam(jam, messageResolver, locale)).toList()));
        return embed.build();
    }

    private static String formatJam(Jam jam, MessageResolver messageResolver, DiscordLocale locale) {
        return "`" + jam.id() + " | " + jam.meta().topic() +  " |`" + getJamState(jam, messageResolver, locale);
    }

    private static String getJamState(Jam jam, MessageResolver messageResolver, DiscordLocale locale) {
        if (jam.state().active()) {
            return messageResolver.resolve("word-active", locale);
        }
        if (jam.state().ended()) {
            return messageResolver.resolve("word-ended", locale);
        }

        return messageResolver.resolve("word-upcoming", locale) + " " + TimeFormat.DATE_TIME_LONG.format(ZonedDateTime.of(jam.time().jamStart(), ZoneId.of(jam.time().zoneId())));
    }

    public static MessageEmbed buildSettingsInfoEmbed(SettingsEntity settings, MessageResolver messageResolver, DiscordLocale userLocale) {
        var embed = new EmbedBuilder();
        embed.setTitle(messageResolver.resolve("command-settings-info-embed-settings", userLocale));
        embed.addField(messageResolver.resolve("command-settings-info-embed-jamrole", userLocale), MentionUtil.role(settings.getParticipantRole()), true);
        embed.addField(messageResolver.resolve("command-settings-info-embed-teamsize", userLocale), String.valueOf(settings.getTeamSize()), true);
        embed.addField(messageResolver.resolve("command-settings-info-embed-orgarole", userLocale), MentionUtil.role(settings.getOrgaRole()), true);
        embed.setColor(Color.CYAN);
        return embed.build();
    }
}
