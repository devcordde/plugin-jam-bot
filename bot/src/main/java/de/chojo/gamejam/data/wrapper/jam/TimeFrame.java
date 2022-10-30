/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.jam;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record TimeFrame(ZonedDateTime start, ZonedDateTime end) {
    public TimeFrame {
        if (start.isAfter(end)) {
            throw new IllegalStateException("Start time is after end time.");
        }
    }

    public static TimeFrame fromEpoch(long start, long end, ZoneId zone) {
        return new TimeFrame(ZonedDateTime.ofInstant(Instant.ofEpochSecond(start), zone),
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(end), zone));
    }

    public static TimeFrame fromTimestamp(Timestamp start, Timestamp end, ZoneId zoneId) {
        return new TimeFrame(ZonedDateTime.ofInstant(Instant.ofEpochMilli(start.getTime()), zoneId),
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(end.getTime()), zoneId));
    }

    public long epochStart() {
        return start.toEpochSecond();
    }

    public long epochEnd() {
        return end.toEpochSecond();
    }

    public boolean contains(ZonedDateTime time) {
        return time.isAfter(start) && time.isBefore(end) || time.isEqual(start) || time.isEqual(end);
    }

    public Timestamp startTimestamp() {
        return Timestamp.from(Instant.ofEpochSecond(epochStart()));
    }

    public Timestamp endTimestamp() {
        return Timestamp.from(Instant.ofEpochSecond(epochEnd()));
    }
}
