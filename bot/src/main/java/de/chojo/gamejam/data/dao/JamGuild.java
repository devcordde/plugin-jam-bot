/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.data.dao;

import de.chojo.gamejam.data.dao.guild.JamSettings;
import de.chojo.gamejam.data.dao.guild.Jams;
import de.chojo.gamejam.data.dao.guild.Settings;
import de.chojo.gamejam.data.dao.guild.Teams;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;

public class JamGuild extends QueryFactory {
    private Guild guild;
    private final Jams jams;
    private final Teams teams;

    public JamGuild(DataSource dataSource, Guild guild) {
        super(dataSource);
        this.guild = guild;
        jams = new Jams(this);
        teams = new Teams(this);
    }

    public Settings settings() {
        return builder(Settings.class)
                .query("SELECT manager_role, locale FROM settings WHERE guild_id  = ?")
                .parameter(p -> p.setLong(guild.getIdLong()))
                .readRow(r -> new Settings(this, guild.getIdLong(), r.getString("locale"), r.getLong("manager_role")))
                .firstSync()
                .orElseGet(() -> new Settings(this, guild.getIdLong()));
    }

    public JamSettings jamSettings() {
        return builder(JamSettings.class)
                .query("SELECT jam_role, team_size FROM jam_settings WHERE guild_id = ?")
                .parameter(p -> p.setLong(guild.getIdLong()))
                .readRow(r -> new JamSettings(this, r.getInt("team_size"), r.getLong("jam_role")))
                .firstSync()
                .orElseGet(() -> new JamSettings(this));
    }

    public JamGuild refresh(Guild guild){
        this.guild = guild;
        return this;
    }

    public Jams jams() {
        return jams;
    }

    public Teams teams() {
        return teams;
    }

    public Guild guild() {
        return guild;
    }

    public long guildId() {
        return guild.getIdLong();
    }
}
