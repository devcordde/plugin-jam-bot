/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.servers;

public class Registration {
    private String name;
    private int port;

    public String name() {
        return name.toLowerCase();
    }

    public int port() {
        return port;
    }
}
