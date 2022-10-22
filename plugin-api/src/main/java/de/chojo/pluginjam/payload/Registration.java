/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.payload;

public class Registration {
    private String name;
    private int port;

    public Registration() {
    }

    public Registration(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String name() {
        return name.toLowerCase();
    }

    public int port() {
        return port;
    }
}
