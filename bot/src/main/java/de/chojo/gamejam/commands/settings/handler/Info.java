/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.JamSettings;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Info implements SlashHandler {
    private final Guilds guilds;

    public Info(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        JamGuild guild = guilds.guild(event.getGuild());
        JamSettings jamSettings = guild.jamSettings();
        var settings = guild.settings();
        var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.settings.info.embed.settings")
                .addField("command.settings.info.embed.jamrole", MentionUtil.role(jamSettings.jamRole()), true)
                .addField("command.settings.info.embed.teamsize", String.valueOf(jamSettings.teamSize()), true)
                .addField("command.settings.info.embed.orgarole", MentionUtil.role(settings.orgaRole()), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
