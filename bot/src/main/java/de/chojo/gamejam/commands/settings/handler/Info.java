/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings.handler;

import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.JamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Info implements SlashHandler {
    private final JamData jamData;
    private final GuildData guildData;

    public Info(JamData jamData, GuildData guildData) {
        this.jamData = jamData;
        this.guildData = guildData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = jamData.getJamSettings(event.getGuild());
        var guildSettings = guildData.getSettings(event.getGuild());
        var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.settings.info.embed.settings")
                .addField("command.settings.info.embed.jamrole", MentionUtil.role(settings.jamRole()), true)
                .addField("command.settings.info.embed.teamsize", String.valueOf(settings.teamSize()), true)
                .addField("command.settings.info.embed.orgarole", MentionUtil.role(guildSettings.orgaRole()), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
