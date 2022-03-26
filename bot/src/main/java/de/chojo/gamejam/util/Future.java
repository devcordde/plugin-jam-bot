/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.util;

import org.slf4j.Logger;

import java.util.function.BiConsumer;

import static org.slf4j.LoggerFactory.getLogger;

public final class Future {
    private static final Logger log = getLogger(Future.class);

    private Future() {
    }

    public static Object log(Runnable run) {
        run.run();
        return null;
    }

    public static void error(Throwable err) {
        log.error("Unhandled Exception occured", err);
    }

    public static <T> BiConsumer<T, Throwable> error() {
        return (value, err) -> {
            if (err != null) {
                log.error("Unhandled Exception occured", err);
            }
        };
    }
}
