/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Plugins {
    private String pluginDir = "plugins";
    private Map<String, String> plugins = new HashMap<>();

    public String pluginDir() {
        return pluginDir;
    }

    public Map<String, String> plugins() {
        return plugins;
    }
}
