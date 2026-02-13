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
import de.chojo.jdautil.configuration.BaseConfiguration;

public class Configuration extends BaseConfiguration<ConfigFile> {
    private Configuration() {
        super(new ConfigFile());
    }

    public static Configuration create() {
        var configuration = new Configuration();
        configuration.reload();
        return configuration;
    }


    public Database database() {
        return config().database();
    }

    public BaseSettings baseSettings() {
        return config().baseSettings();
    }

    public Api api() {
        return config().api();
    }

    public ServerManagement serverManagement() {
        return config().serverManagement();
    }

    public Plugins plugins() {
        return config().plugins();
    }

    public ServerTemplate serverTemplate() {
        return config().serverTemplate();
    }
}
