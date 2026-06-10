/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import com.google.inject.Inject;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.JamSettings;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.EventContext;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Bundle("locale")
@Interaction
public final class InfoCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public InfoCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "settings info")
    public void onCommand(CommandEvent event) {
        var jamGuild = commandContextProvider.guilds().guild(event.getGuild());
        var jamSettings = jamGuild.jamSettings();

        var settings = jamGuild.settings();

        var embed = event.embed("settings-info");
        embed.title("command.settings.info.embed.settings");
        embed.fields().add("command.settings.info.embed.jamrole", MentionUtil.role(jamSettings.jamRole()), true);
        embed.fields().add("command.settings.info.embed.teamsize", String.valueOf(jamSettings.teamSize()), true);
        embed.fields().add("command.settings.info.embed.orgarole", MentionUtil.role(settings.orgaRole()), true);

        event.reply(embed.build());
    }
}
