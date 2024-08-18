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
import net.dv8tion.jda.api.entities.Guild;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class JamGuild {
    private final long guildId;
    private Guild guild;
    private final Jams jams;
    private final Teams teams;

    public JamGuild(Guild guild) {
        this.guild = guild;
        this.guildId = guild.getIdLong();
        jams = new Jams(this);
        teams = new Teams(this);
    }

    public JamGuild(long guild) {
        this.guild = null;
        this.guildId = guild;
        jams = new Jams(this);
        teams = new Teams(this);
    }

    public Settings settings() {
        return query("SELECT manager_role, locale FROM settings WHERE guild_id  = ?")
                .single(call().bind(guild.getIdLong()))
                .map(r -> new Settings(guild.getIdLong(), r.getString("locale"), r.getLong("manager_role")))
                .first()
                .orElseGet(() -> new Settings(guild.getIdLong()));
    }

    public JamSettings jamSettings() {
        return query("SELECT jam_role, team_size FROM jam_settings WHERE guild_id = ?")
                .single(call().bind(guild.getIdLong()))
                .map(r -> new JamSettings(this, r.getInt("team_size"), r.getLong("jam_role")))
                .first()
                .orElseGet(() -> new JamSettings(this));
    }

    public JamGuild refresh(Guild guild) {
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
        return guildId;
    }
}
