/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.configuration.elements;

public class Api {
    private String host = "localhost";
    private int port = 25000;

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }
}
