/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.payload;

public record StatsPayload(
        double[] tps,
        double averageTickTime,
        int onlinePlayers,
        int activeThreads,
        Memory memory
) {
    public record Memory(long totalMb,
                         long freeMb,
                         long usedMb,
                         long maxMb) {
    }
}
