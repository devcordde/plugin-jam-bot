/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;


@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Database {
    private String host = "localhost";
    private String port = "5432";
    private String database = "db";
    private String schema = "bot";
    private String user = "root";
    private String password = "changeme";
    private int poolSize = 5;

    public String host() {
        return host;
    }

    public String port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String schema() {
        return schema;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public int poolSize() {
        return poolSize;
    }
}
