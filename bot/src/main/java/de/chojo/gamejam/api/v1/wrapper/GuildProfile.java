package de.chojo.gamejam.api.v1.wrapper;

import net.dv8tion.jda.api.entities.Guild;

public record GuildProfile(String name, String iconUrl, long idLong) {
    public static GuildProfile build(Guild guild) {
        var name = guild.getName();
        var iconUrl = guild.getIconUrl();
        var idLong = guild.getIdLong();
        return new GuildProfile(name, iconUrl, idLong);
    }
}
