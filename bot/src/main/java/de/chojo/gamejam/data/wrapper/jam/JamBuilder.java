/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import de.chojo.gamejam.data.dao.guild.jams.jam.JamTimes;

public class JamBuilder {
    private JamTimes times;
    private String topic;

    public JamBuilder setTimes(JamTimes times) {
        this.times = times;
        return this;
    }

    public JamBuilder setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public JamCreator build() {
        return new JamCreator(times, topic);
    }
}
