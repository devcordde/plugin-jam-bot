/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

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
