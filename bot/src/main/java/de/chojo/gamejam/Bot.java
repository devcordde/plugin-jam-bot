/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam;

import de.chojo.gamejam.commands.JamAdmin;
import de.chojo.gamejam.commands.Register;
import de.chojo.gamejam.commands.Settings;
import de.chojo.gamejam.commands.Team;
import de.chojo.gamejam.commands.Unregister;
import de.chojo.gamejam.commands.Votes;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.gamejam.util.LogNotify;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.logging.LoggerAdapter;
import de.chojo.sqlutil.updater.QueryReplacement;
import de.chojo.sqlutil.updater.SqlType;
import de.chojo.sqlutil.updater.SqlUpdater;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private CommandHub<SimpleCommand> commandHub;
    private QueryBuilderConfig config;
    private JamData jamData;
    private TeamData teamData;
    private GuildData guildData;

    private static ThreadFactory createThreadFactory(String string) {
        return createThreadFactory(new ThreadGroup(string));
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

    private ExecutorService createExecutor(int threads, String name) {
        return Executors.newFixedThreadPool(threads, createThreadFactory(name));
    }

    public void start() throws IOException, SQLException, LoginException {
        configuration = Configuration.create();

        initDb();

        initBot();

        buildLocale();

        buildCommands();
    }

    private void buildLocale() {
        localizer = Localizer.builder(Language.ENGLISH)
                .addLanguage(Language.GERMAN)
                .withLanguageProvider(guild -> Optional.ofNullable(guildData.getSettings(guild).join().locale()))
                .build();
    }

    private void buildCommands() {
        var settings = new Settings(jamData, guildData);
        commandHub = CommandHub.builder(shardManager)
                .withManagerRole(guild -> Collections.singletonList(guildData.getSettings(guild).join().orgaRole()))
                .withLocalizer(localizer)
                .useGuildCommands()
                .withCommands(new JamAdmin(jamData),
                        new Register(jamData),
                        settings,
                        new Team(teamData, jamData),
                        new Unregister(jamData, teamData),
                        new Votes(jamData, teamData))
                .withPagination(builder -> builder.withLocalizer(localizer).withCache(cache -> cache.expireAfterAccess(30, TimeUnit.MINUTES)))
                .withButtonService(builder -> builder.withLocalizer(localizer).withCache(cache -> cache.expireAfterAccess(30, TimeUnit.MINUTES)))
                .build();
        settings.init(commandHub);
    }

    private void initBot() throws LoginException {
        shardManager = DefaultShardManagerBuilder.createDefault(configuration.baseSettings().token())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MESSAGES)
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                .build();
        RestAction.setDefaultFailure(throwable -> log.error("Unhandled exception occured: ", throwable));
    }

    private void initDb() throws IOException, SQLException {
        dataSource = DataSourceCreator.create(PGSimpleDataSource.class)
                .withAddress(configuration.database().host())
                .withPort(configuration.database().port())
                .forDatabase(configuration.database().database())
                .withUser(configuration.database().user())
                .withPassword(configuration.database().password())
                .create()
                .forSchema(configuration.database().schema())
                .build();

        SqlUpdater.builder(dataSource, SqlType.POSTGRES)
                .withLogger(LoggerAdapter.wrap(log))
                .setReplacements(new QueryReplacement("gamejam", configuration.database().schema()))
                .execute();

        config = QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err), err))
                .withExecutor(createExecutor("DataWorker"))
                .build();
        jamData = new JamData(dataSource, config);
        teamData = new TeamData(dataSource, config);
        guildData = new GuildData(dataSource, config);
    }
}
