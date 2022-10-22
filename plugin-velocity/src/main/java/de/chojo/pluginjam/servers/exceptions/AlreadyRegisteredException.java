/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.servers.exceptions;

public class AlreadyRegisteredException extends RuntimeException {
    private AlreadyRegisteredException(String message) {
        super(message);
    }

    public static AlreadyRegisteredException forName(String name) {
        return new AlreadyRegisteredException("Name " + name + " is already in use.");
    }

    public static AlreadyRegisteredException forPort(int port) {
        return new AlreadyRegisteredException("Port " + port + " is already in use.");
    }
}
