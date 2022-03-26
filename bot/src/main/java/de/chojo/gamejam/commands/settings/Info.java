/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Info implements SubCommand<JamSettings> {
    private final GuildData guildData;

    public Info(GuildData guildData) {
        this.guildData = guildData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var guildSettings = guildData.getSettings(event.getGuild()).join();
        var embed = new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.settings.info.settings")
                .addField("command.settings.info.jamRole", MentionUtil.role(settings.jamRole()), true)
                .addField("command.settings.info.teamSize", String.valueOf(settings.teamSize()), true)
                .addField("command.settings.info.orgaRole", MentionUtil.role(guildSettings.orgaRole()), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
