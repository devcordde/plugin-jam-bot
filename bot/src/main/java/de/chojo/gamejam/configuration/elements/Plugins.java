/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration.elements;

import de.chojo.jdautil.container.Pair;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Plugins {
    private String pluginDir = "plugins";

    public String pluginDir() {
        return pluginDir;
    }

    private Path pluginPath() {
        return Path.of(pluginDir);
    }

    public List<File> pluginFiles() {
        return List.of(pluginPath().toFile().listFiles(File::isFile));
    }

    public List<String> pluginNames() {
        return pluginFiles().stream()
                            .map(File::getName)
                            .map(name -> name.replace(".jar", "")).toList();
    }

    public List<Pair<String, File>> plugins() {
        return pluginFiles().stream()
                            .map(file -> Pair.of(file.getName().replace(".jar", ""), file))
                            .toList();
    }

    public Optional<Path> byName(String pluginName) {
        return plugins().stream().filter(plugin -> plugin.first.equals(pluginName))
                        .findFirst()
                        .map(plugin -> plugin.second.toPath());
    }
}
