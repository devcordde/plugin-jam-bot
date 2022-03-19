/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class JamData extends QueryFactoryHolder {
    private static final Logger log = getLogger(JamData.class);

    public JamData(DataSource dataSource, QueryBuilderConfig config) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err), err))
                .build());
    }
}
