/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration;

import de.chojo.gamejam.configuration.elements.Api;
import de.chojo.gamejam.configuration.elements.BaseSettings;
import de.chojo.gamejam.configuration.elements.Database;
import de.chojo.gamejam.configuration.elements.Plugins;
import de.chojo.gamejam.configuration.elements.ServerManagement;
import de.chojo.gamejam.configuration.elements.ServerTemplate;

@SuppressWarnings("FieldMayBeFinal")
public class ConfigFile {
    private BaseSettings baseSettings = new BaseSettings();
    private Database database = new Database();
    private Api api = new Api();
    private ServerManagement serverManagement = new ServerManagement();
    private Plugins plugins = new Plugins();
    private ServerTemplate serverTemplate = new ServerTemplate();

    public BaseSettings baseSettings() {
        return baseSettings;
    }

    public Database database() {
        return database;
    }

    public Api api() {
        return api;
    }

    public ServerManagement serverManagement(){
        return serverManagement;
    }

    public Plugins plugins() {
        return plugins;
    }

    public ServerTemplate serverTemplate() {
        return serverTemplate;
    }
}
