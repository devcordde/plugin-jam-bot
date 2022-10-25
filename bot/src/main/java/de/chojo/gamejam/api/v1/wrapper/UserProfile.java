/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.api.v1.wrapper;

import net.dv8tion.jda.api.entities.Member;

public record UserProfile(String name, String tag, String iconUrl, long idLong) {
    public static UserProfile build(Member guild) {
        var name = guild.getEffectiveName();
        var tag = guild.getUser().getAsTag();
        var iconUrl = guild.getEffectiveAvatarUrl();
        var idLong = guild.getIdLong();
        return new UserProfile(name, tag, iconUrl, idLong);
    }
}
