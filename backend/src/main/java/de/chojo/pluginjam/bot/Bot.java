/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot;

import com.google.inject.Guice;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import de.chojo.pluginjam.bot.listener.InviteButtonListener;
import de.chojo.pluginjam.database.repository.SettingsRepository;
import de.chojo.pluginjam.service.JamService;
import de.chojo.pluginjam.service.SettingsService;
import de.chojo.pluginjam.service.TeamService;
import de.chojo.pluginjam.service.VoteService;
import io.github.kaktushose.jdac.JDACommands;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.CommandScope;
import io.github.kaktushose.jdac.definitions.interactions.command.CommandDefinition;
import io.github.kaktushose.jdac.guice.GuiceExtensionData;
import io.github.kaktushose.jdac.scope.GuildScopeProvider;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class Bot implements ApplicationEventListener<ApplicationStartupEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(Bot.class);
    private final JamService jamService;
    private final SettingsService settingsService;
    private final TeamService teamService;
    private final VoteService voteService;
    private final String token;

    private ShardManager shardManager;

    public Bot(JamService jamService, SettingsService settingsService, TeamService teamService, VoteService voteService, @Value("${bot.token}") String token) {
        this.jamService = jamService;
        this.settingsService = settingsService;
        this.teamService = teamService;
        this.voteService = voteService;
        this.token = token;
    }

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        try {
            start();
        } catch (Exception e) {
            LOG.error("Failed to start bot", e);
            throw new RuntimeException(e);
        }
    }

    public void start() {

        LOG.info("Initializing Shard Manager");
        initBot();

        LOG.info("Initializing Commands");
        buildCommands();

        buildShutdownHook();
    }

    private void buildShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Received SIGTERM. Shutdown hook activated.");
            shardManager.shutdown();
        }));
    }

    private void buildCommands() {
        var userContextProvider = new CommandContextProvider(
                jamService,
                settingsService,
                teamService,
                voteService
        );
        var injector = Guice.createInjector(new BotModule(userContextProvider));
        JDACommands.builder(shardManager)
                .extensionData(new GuiceExtensionData(injector))
                .globalCommandConfig(CommandDefinition.CommandConfig.of(builder -> {
                    builder.scope(CommandScope.GUILD);
                }))
                .guildScopeProvider(new GuildScopeProvider() {
                    @Override
                    public @NonNull Set<Long> apply(@NonNull CommandData commandData) {
                        return shardManager.getGuilds().stream()
                                .map(Guild::getIdLong)
                                .collect(Collectors.toSet());
                    }
                })
                .start();
        shardManager.addEventListener(new InviteButtonListener(teamService, settingsService, jamService));
    }

    private void initBot() {
        shardManager = DefaultShardManagerBuilder.createDefault(token)
                .setEnableShutdownHook(false)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MESSAGES)
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                .build();
        RestAction.setDefaultFailure(throwable -> LOG.error("Unhandled exception occurred: ", throwable));
    }
}
