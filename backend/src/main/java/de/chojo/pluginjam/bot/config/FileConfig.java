/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
@ConfigurationProperties("files")
public class FileConfig {
    private List<FileRule> rules = new ArrayList<>();

    public List<FileRule> getRules() {
        return rules;
    }

    public void setRules(List<FileRule> rules) {
        this.rules = rules;
    }

    @Serdeable
    public static class FileRule {
        private String pattern;
        private boolean readOnly = true;
        private boolean show = true;

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }

        public boolean isReadOnly() { return readOnly; }
        public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

        public boolean isShow() { return show; }
        public void setShow(boolean show) { this.show = show; }
    }
}