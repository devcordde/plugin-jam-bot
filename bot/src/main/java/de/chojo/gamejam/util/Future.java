/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.util;

import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import org.slf4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public final class Future {
    private static final Logger log = getLogger(Future.class);

    private Future() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static Object log(Runnable run) {
        run.run();
        return null;
    }

    public static void error(Throwable err) {
        log.error("Unhandled Exception occured", err);
    }

    public static Consumer<Throwable> error() {
      return   err -> {
        log.error("Unhandled Exception occured", err);
        };
    }

    public static <T> BiConsumer<T, Throwable> handleComplete() {
        return (value, err) -> {
            if (err != null) {
                log.error("Unhandled Exception occured", err);
            }
        };
    }

    public static  <T, E extends Throwable> java.util.function.BiConsumer<? super T, E> handle(Consumer<T> result, Consumer<E> err) {
        return (BiConsumer<T, E>) (t, throwable) -> {
            if (throwable != null) {
                err.accept(throwable);
                return;
            }
            result.accept(t);
        };
    }
}
