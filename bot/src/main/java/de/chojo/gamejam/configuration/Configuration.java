/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.gamejam.configuration.elements.Api;
import de.chojo.gamejam.configuration.elements.BaseSettings;
import de.chojo.gamejam.configuration.elements.Database;
import de.chojo.gamejam.configuration.elements.ServerManagement;
import de.chojo.gamejam.configuration.exception.ConfigurationException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.slf4j.LoggerFactory.getLogger;

public class Configuration {
    private static final Logger log = getLogger(Configuration.class);
    private final ObjectMapper objectMapper;
    private ConfigFile configFile;

    private Configuration() {
        objectMapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .setDefaultPrettyPrinter(new DefaultPrettyPrinter())
                .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true);
    }

    public static Configuration create() {
        var configuration = new Configuration();
        configuration.reload();
        return configuration;
    }

    public void reload() {
        try {
            reloadFile();
        } catch (IOException e) {
            log.info("Could not load config", e);
            throw new ConfigurationException("Could not load config file", e);
        }
        try {
            save();
        } catch (IOException e) {
            log.error("Could not save config.", e);
        }
    }

    private void save() throws IOException {
        try (var sequenceWriter = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValues(getConfig().toFile())) {
            sequenceWriter.write(configFile);
        }
    }

    private void reloadFile() throws IOException {
        forceConsistency();
        configFile = objectMapper.readValue(getConfig().toFile(), ConfigFile.class);
    }

    private void forceConsistency() throws IOException {
        Files.createDirectories(getConfig().getParent());
        if (!getConfig().toFile().exists()) {
            if (getConfig().toFile().createNewFile()) {
                try(var sequenceWriter = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValues(getConfig().toFile())) {
                    sequenceWriter.write(new ConfigFile());
                    throw new ConfigurationException("Please configure the config.");
                }
            }
        }
    }

    private Path getConfig() {
        var home = new File(".").getAbsoluteFile().getParentFile().toPath();
        var property = System.getProperty("bot.config");
        if (property == null) {
            log.error("bot.config property is not set.");
            throw new ConfigurationException("Property -Dbot.config=<config path> is not set.");
        }
        return Paths.get(home.toString(), property);
    }

    public Database database() {
        return configFile.database();
    }

    public BaseSettings baseSettings() {
        return configFile.baseSettings();
    }

    public Api api() {
        return configFile.api();
    }

    public ServerManagement serverManagement() {
        return configFile.serverManagement();
    }
}
