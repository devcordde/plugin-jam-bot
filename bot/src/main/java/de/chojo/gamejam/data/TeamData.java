/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamData extends QueryFactoryHolder {

    private static final Logger log = getLogger(TeamData.class);

    public TeamData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err), err))
                .build());
    }

    public void createTeam(Guild guild, Member founder, String name) {

    }
}
