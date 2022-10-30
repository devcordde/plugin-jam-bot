/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ServerTemplate {
    private String templateDir = "template";
    private List<String> symLinks = List.of(
            "bukkit.yml",
            "commands.yml",
            "config/paper-global.yml",
            "config/paper-world-defaults.yml",
            "help.yml",
            "permissions.yml",
            "plugins/pluginjam.jar",
            "server-icon.png",
            "server.jar",
            "server.properties",
            "spigot.yml",
            "wepif.yml");

    public String templateDir() {
        return templateDir;
    }

    public List<String> symLinks() {
        return symLinks;
    }
}
