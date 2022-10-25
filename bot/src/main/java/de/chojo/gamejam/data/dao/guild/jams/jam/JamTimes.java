/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao.guild.jams.jam;

import de.chojo.gamejam.data.wrapper.jam.TimeFrame;

import java.time.ZoneId;

public class JamTimes {
    private final ZoneId zone;
    private final TimeFrame total;
    private final TimeFrame registration;
    private final TimeFrame jam;


    public JamTimes(ZoneId zone, TimeFrame registration, TimeFrame jam) {
        this.zone = zone;
        this.registration = registration;
        this.jam = jam;
        total = new TimeFrame(registration.start(), jam.end());
    }

    public TimeFrame total() {
        return total;
    }

    public TimeFrame registration() {
        return registration;
    }

    public TimeFrame jam() {
        return jam;
    }

    public ZoneId zone() {
        return zone;
    }
}
