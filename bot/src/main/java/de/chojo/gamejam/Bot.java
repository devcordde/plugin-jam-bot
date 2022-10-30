/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam;

import de.chojo.gamejam.api.Api;
import de.chojo.gamejam.commands.jamadmin.JamAdmin;
import de.chojo.gamejam.commands.register.Register;
import de.chojo.gamejam.commands.server.Server;
import de.chojo.gamejam.commands.serveradmin.ServerAdmin;
import de.chojo.gamejam.commands.settings.Settings;
import de.chojo.gamejam.commands.team.Team;
import de.chojo.gamejam.commands.unregister.Unregister;
import de.chojo.gamejam.commands.vote.Votes;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.access.Teams;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.util.LogNotify;
import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.sadu.databases.PostgreSql;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.exceptions.ExceptionTransformer;
import de.chojo.sadu.mapper.PostgresqlMapper;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot {
    private static final Logger log = getLogger(Bot.class);
    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (t, e) -> log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occured in " + t.getName() + "-" + t.getId() + ".", e);

    private static final Bot instance;

    static {
        instance = new Bot();
    }

    private Configuration configuration;
    private DataSource dataSource;
    private ILocalizer localizer;
    private ShardManager shardManager;
    private Guilds guilds;
    private ServerService serverService;

    private static ThreadFactory createThreadFactory(String name) {
        return createThreadFactory(new ThreadGroup(name));
    }

    private static ThreadFactory createThreadFactory(ThreadGroup group) {
        return r -> {
            var thread = new Thread(group, r, group.getName());
            thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
            return thread;
        };
    }

    public static void main(String[] args) {
        try {
            instance.start();
        } catch (Exception e) {
            log.error("Critical error occured during bot startup", e);
        }
    }

    private ExecutorService createExecutor(String name) {
        return Executors.newCachedThreadPool(createThreadFactory(name));
    }

    private ScheduledExecutorService createScheduledExecutor(String name, int size) {
        return Executors.newScheduledThreadPool(10, createThreadFactory(name));
    }

    private ExecutorService createExecutor(int threads, String name) {
        return Executors.newFixedThreadPool(threads, createThreadFactory(name));
    }

    public void start() throws IOException, SQLException, LoginException {
        configuration = Configuration.create();

        initDb();

        initServer();

        initBot();

        buildLocale();

        buildCommands();

        Api.create(configuration, shardManager, guilds);

    }

    private void buildLocale() {
        localizer = Localizer.builder(DiscordLocale.ENGLISH_US)
                .addLanguage(DiscordLocale.GERMAN)
                .withLanguageProvider(guild -> Optional.ofNullable(guilds.guild(guild).settings().locale())
                                                       .map(DiscordLocale::from))
                .build();
    }

    private void buildCommands() {
        InteractionHub.builder(shardManager)
                .withLocalizer(localizer)
                .withCommands(new JamAdmin(guilds),
                        new Register(guilds),
                        new Settings(guilds),
                        new Team(guilds),
                        new Unregister(guilds),
                        new Votes(guilds),
                        new Server(guilds, serverService, configuration),
                        new ServerAdmin(guilds, serverService))
                .withPagination(builder -> builder.withLocalizer(localizer)
                                                  .withCache(cache -> cache.expireAfterAccess(30, TimeUnit.MINUTES)))
                .withMenuService(builder -> builder.withLocalizer(localizer)
                                                   .withCache(cache -> cache.expireAfterAccess(30, TimeUnit.MINUTES)))
                .withModalService(builder -> builder.withLocalizer(localizer))
                .build();
    }

    private void initBot() {
        shardManager = DefaultShardManagerBuilder.createDefault(configuration.baseSettings().token())
                                                 .enableIntents(
                                                         GatewayIntent.GUILD_MEMBERS,
                                                         GatewayIntent.DIRECT_MESSAGES,
                                                         GatewayIntent.GUILD_MESSAGES)
                                                 .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                                                 .setEventPool(Executors.newScheduledThreadPool(5, createThreadFactory("Event Worker")))
                                                 .build();
        RestAction.setDefaultFailure(throwable -> log.error("Unhandled exception occured: ", throwable));
        serverService.inject(new Teams(dataSource, guilds, shardManager));
        serverService.syncVelocity();
    }

    private void initDb() throws IOException, SQLException {
        var mapperRegistry = new RowMapperRegistry();
        mapperRegistry.register(PostgresqlMapper.getDefaultMapper());
        QueryBuilderConfig.setDefault(QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err), err))
                .withExecutor(createExecutor("DataWorker"))
                .rowMappers(mapperRegistry)
                .build());

        dataSource = DataSourceCreator.create(PostgreSql.get())
                .configure(config -> {
                    config.host(configuration.database().host())
                          .port(configuration.database().port())
                          .database(configuration.database().database())
                          .user(configuration.database().user())
                          .password(configuration.database().password());
                })
                .create()
                .forSchema(configuration.database().schema())
                .build();

        SqlUpdater.builder(dataSource, PostgreSql.get())
                .setReplacements(new QueryReplacement("gamejam", configuration.database().schema()))
                .setSchemas(configuration.database().schema())
                .execute();

        guilds = new Guilds(dataSource);
    }

    private void initServer() throws IOException {
        serverService = ServerService.create(createScheduledExecutor("Server ping", 1), configuration);

        var templateDir = Path.of(configuration.serverTemplate().templateDir());
        var serverDir = Path.of(configuration.serverManagement().serverDir());
        var pluginDir = Path.of(configuration.plugins().pluginDir());

        Files.createDirectories(templateDir);
        Files.createDirectories(serverDir);
        Files.createDirectories(pluginDir);

        Path wait = Path.of("wait.sh");
        Files.copy(getClass().getClassLoader().getResourceAsStream("wait.sh"), wait, StandardCopyOption.REPLACE_EXISTING);
        Files.setPosixFilePermissions(wait, Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
    }
}
