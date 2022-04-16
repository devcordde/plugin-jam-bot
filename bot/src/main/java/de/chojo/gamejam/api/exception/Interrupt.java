/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.exception;

import io.javalin.http.HttpCode;
import org.jetbrains.annotations.Contract;

public final class Interrupt {
    private Interrupt() {
    }

    public static InterruptException create(String message, HttpCode httpCode) {
        return new InterruptException(message, httpCode);
    }

    @Contract("_ -> fail")
    public static InterruptException notFound(String entity) {
        return create(String.format("%s not found.", entity), HttpCode.NOT_FOUND);
    }

    @Contract("null,_ -> fail")
    public static void assertNotFound(Object object, String entity) throws InterruptException {
        assertNotFound(object == null, entity);
    }

    @Contract("true,_ -> fail")
    public static void assertNotFound(boolean failed, String entity) throws InterruptException {
        if (failed) throw notFound(entity);
    }

    @Contract(" -> fail")
    public static InterruptException noJam() {
        return create("No current or upcoming jam", HttpCode.NOT_FOUND);
    }

    @Contract("true -> fail")
    public static void assertNoJam(boolean failed) throws InterruptException {
        if (failed) {
            throw noJam();
        }
    }
}
