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
import de.chojo.gamejam.commands.Vote;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import de.chojo.sqlutil.logging.LoggerAdapter;
import de.chojo.sqlutil.updater.QueryReplacement;
import de.chojo.sqlutil.updater.SqlType;
import de.chojo.sqlutil.updater.SqlUpdater;
import net.dv8tion.jda.api.requests.GatewayIntent;
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
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot {

    private static final Bot instance;
    private Configuration configuration;
    private DataSource dataSource;
    private Localizer localizer;
    private ShardManager shardManager;
    private CommandHub<SimpleCommand> commandHub;

    private static final Logger log = getLogger(Bot.class);

    static {
        instance = new Bot();
    }

    public static void main(String[] args) {
        try {
            instance.start();
        } catch (Exception e) {
            log.error("Critical error occured during bot startup", e);
        }
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
                //TODO: Implement language
                .withLanguageProvider(guild -> Optional.ofNullable(Language.GERMAN.getCode()))
                .build();
    }

    private void buildCommands() {
        var jamData = new JamData(dataSource);
        var teamData = new TeamData(dataSource);
        commandHub = CommandHub.builder(shardManager)
                //TODO: Implement manager role retrieval
                .withManagerRole(guild -> Collections.emptyList())
                .withLocalizer(localizer)
                .useGuildCommands()
                //TODO: Add Commands
                .withCommands(new JamAdmin(jamData),
                        new Register(jamData),
                        new Settings(jamData),
                        new Team(teamData, jamData),
                        new Vote())
                .withPagination(builder -> builder.cache(cache -> cache.expireAfterAccess(30, TimeUnit.MINUTES)))
                .withButtonService(builder -> builder.setCache(cache -> cache.expireAfterAccess(30, TimeUnit.MINUTES)))
                .build();
    }

    private void initBot() throws LoginException {
        shardManager = DefaultShardManagerBuilder.createDefault(configuration.baseSettings().token())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MESSAGES)
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                .build();
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
    }
}
