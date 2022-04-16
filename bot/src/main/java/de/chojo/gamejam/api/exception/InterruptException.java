/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.exception;

import io.javalin.http.HttpCode;

public class InterruptException extends Exception {
    private final HttpCode status;

    public InterruptException(String message, HttpCode status) {
        super(message);
        this.status = status;
    }

    public InterruptException(HttpCode status) {
        super(status.getMessage());
        this.status = status;
    }

    public int status() {
        return status.getStatus();
    }
}
