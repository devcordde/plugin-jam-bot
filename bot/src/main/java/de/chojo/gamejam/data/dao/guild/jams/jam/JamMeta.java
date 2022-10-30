/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam;

public class JamMeta {
    private final String topic;

    public JamMeta(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return topic;
    }
}
