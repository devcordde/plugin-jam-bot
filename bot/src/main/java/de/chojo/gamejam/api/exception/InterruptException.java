/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.exception;

import io.javalin.http.HttpStatus;

public class InterruptException extends Exception {
    private final HttpStatus status;

    public InterruptException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public InterruptException(HttpStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    public int status() {
        return status.getCode();
    }
}
