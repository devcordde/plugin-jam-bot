/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.configuration;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import de.chojo.pluginjam.configuration.elements.Api;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.slf4j.LoggerFactory.getLogger;

public class Configuration {
    private static final Logger log = getLogger(Configuration.class);
    private Api api = new Api();

    public Api api() {
        return api;
    }

    public static Configuration load() throws IOException {
        var gson = new Gson();

        var path = Path.of("plugins", "pluginjam", "config.json").toAbsolutePath();

        log.info("Loading {}", path);

        if (!path.toFile().exists()) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(new Configuration()), StandardCharsets.UTF_8);
        }

        var configuration = gson.fromJson(Files.readString(path), Configuration.class);

        Files.writeString(path, gson.toJson(configuration), StandardCharsets.UTF_8);

        return configuration;
    }
}
