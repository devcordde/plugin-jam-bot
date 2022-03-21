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

public record Info(GuildData guildData) implements SubCommand<JamSettings> {
    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var guildSettings = guildData.getSettings(event.getGuild()).join();
        var embed = new LocalizedEmbedBuilder(context.localizer())
                .setTitle("Settings")
                .addField("Game Jam Role", MentionUtil.role(settings.jamRole()), true)
                .addField("Max Team Size", String.valueOf(settings.teamSize()), true)
                .addField("Orga Role", MentionUtil.role(guildSettings.orgaRole()), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
