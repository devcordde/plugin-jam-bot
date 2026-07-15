/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.config;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("plugins")
public class PluginsConfig {
    private List<String> defaultPlugins = List.of();

    public List<String> getDefaultPlugins() {
        return defaultPlugins;
    }
}
