/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.settings;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public final class SettingsTeamSizeCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public SettingsTeamSizeCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "settings teamsize")
    public void onCommand(CommandEvent event, @Param("size") int size) {
        var guildId = event.getGuild().getIdLong();
        commandContextProvider.settingsService().setTeamSize(guildId, size);
        event.with().ephemeral(true).reply("command-settings-teamsize-message-updated");
    }
}
