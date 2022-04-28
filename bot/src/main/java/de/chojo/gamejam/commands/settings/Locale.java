/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.GuildData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Locale implements SubCommand<JamSettings> {
    private final GuildData guildData;

    public Locale(GuildData guildData) {
        this.guildData = guildData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        var locale = event.getOption("locale").getAsString();
        var guildSettings = guildData.getSettings(event.getGuild()).join();
        context.localizer().localizer().languages().stream().filter(lang -> lang.isLanguage(locale)).findFirst()
                .ifPresentOrElse(language -> {
                    guildSettings.locale(language.getCode());
                    guildData.updateSettings(guildSettings)
                            .thenRun(() -> {
                                event.reply(context.localize("command.settings.locale.updated")).setEphemeral(true).queue();
                                context.commandHub().refreshGuildCommands(event.getGuild());
                            });
                }, () -> event.reply(context.localize("command.settings.locale.invalid")).setEphemeral(true).queue());
    }
}
