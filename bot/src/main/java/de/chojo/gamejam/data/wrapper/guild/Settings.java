/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.wrapper.guild;

public class Settings {
    private final long guildId;
    private String locale = "en_US";
    private long orgaRole = 0;

    public Settings(long guildId) {
        this.guildId = guildId;
    }

    public Settings(long guildId, String locale, long orgaRole) {
        this.guildId = guildId;
        this.locale = locale;
        this.orgaRole = orgaRole;
    }

    public String locale() {
        return locale;
    }

    public long orgaRole() {
        return orgaRole;
    }

    public void locale(String locale) {
        this.locale = locale;
    }

    public void orgaRole(long managerRole) {
        orgaRole = managerRole;
    }

    public Long guildId() {
        return guildId;
    }
}
