/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration;

import de.chojo.gamejam.configuration.elements.*;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;

import java.nio.file.Path;
import java.util.List;

public class Configuration extends Configurations<ConfigFile> {
    public static final Key<ConfigFile> MAIN = Key.builder(Path.of("config.yml"), ConfigFile::new).build();

    private Configuration() {
        super(Path.of("config"), MAIN, List.of(new YamlDataFormat()), Configuration.class.getClassLoader(), null);
    }

    public static Configuration create() {
        var configuration = new Configuration();
        configuration.reload();
        return configuration;
    }


    public Database database() {
        return main().database();
    }

    public Docker docker() {
        return main().docker();
    }

    public BaseSettings baseSettings() {
        return main().baseSettings();
    }

    public Api api() {
        return main().api();
    }

    public ServerManagement serverManagement() {
        return main().serverManagement();
    }

    public Plugins plugins() {
        return main().plugins();
    }

    public ServerTemplate serverTemplate() {
        return main().serverTemplate();
    }
}
