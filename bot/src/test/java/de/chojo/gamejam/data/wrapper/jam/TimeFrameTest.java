/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

class TimeFrameTest {
    private static final ZoneId berlin = ZoneId.of("Europe/Berlin");
    private static final ZoneId newyork = ZoneId.of("America/New_York");

    @Test
    void equals() {
        Assertions.assertEquals(TimeFrame.fromEpoch(0, 1000, berlin), TimeFrame.fromEpoch(0, 1000, berlin));
        Assertions.assertNotEquals(TimeFrame.fromEpoch(0, 1500, berlin), TimeFrame.fromEpoch(0, 1000, berlin));
    }

    @Test
    void contains() {
        DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        var instant = LocalDateTime.from(dateParser.parse("2022.03.20 19:18"));
        ZonedDateTime time = ZonedDateTime.ofInstant(LocalDateTime.from(dateParser.parse("2022.03.20 19:18")), berlin.getRules().getOffset(instant), berlin);
        System.out.println(time);
    }

    private static ZonedDateTime time(int hour, ZoneId zoneId) {
        return ZonedDateTime.of(2021, 1, 1, hour, 0, 0, 0, zoneId);
    }

    @Test
    void fromEpoch() {
        var berlinFrame = TimeFrame.fromEpoch(0, 1000, berlin);
        var newyorkFrame = TimeFrame.fromEpoch(0, 1000, newyork);
        Assertions.assertNotEquals(berlinFrame, newyorkFrame);
    }

    @Test
    void fromTimestamp() {
        var frame = TimeFrame.fromEpoch(0, 1000, berlin);
        var start = frame.startTimestamp();
        var end = frame.endTimestamp();

        Assertions.assertEquals(frame, TimeFrame.fromTimestamp(start, end, berlin));
    }
}
