/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Api {

    private String contextPath = "/";
    private String host = "localhost";
    private int port = 8888;
    private String token = "letmein";

    public String contextPath() {
        return contextPath;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String token() {
        return token;
    }
}
