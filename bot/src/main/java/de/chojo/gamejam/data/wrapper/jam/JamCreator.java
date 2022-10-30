/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import de.chojo.gamejam.data.dao.guild.jams.jam.JamTimes;

public record JamCreator(JamTimes times, String topic) {

    public static JamBuilder create() {
        return new JamBuilder();
    }
}
