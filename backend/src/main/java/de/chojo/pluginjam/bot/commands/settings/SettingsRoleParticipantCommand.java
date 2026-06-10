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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Bundle("locale")
@Interaction
public final class SettingsRoleParticipantCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public SettingsRoleParticipantCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "settings role participant")
    public void onCommand(CommandEvent event, @Param(value = "role", type = OptionType.ROLE) Role role) {
        var guildId = event.getGuild().getIdLong();
        commandContextProvider.settingsService().setJamRole(guildId, role.getIdLong());
        event.with().ephemeral(true).reply("command-settings-jamrole-message-updated");
    }
}
