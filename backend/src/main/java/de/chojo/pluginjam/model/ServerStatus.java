/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.model;

public enum ServerStatus {
    STARTING_STOPPING("⏳"),
    RUNNING("🟢"),
    STOPPED("🔴"),
    VOID("❌");

    private final String emoji;
    ServerStatus(String emoji) {
        this.emoji = emoji;
    }

    public String emoji() {
        return emoji;
    }
}
