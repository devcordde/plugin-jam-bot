/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.jamadmin;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.CommandScope;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public final class JamStartCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public JamStartCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "jamadmin start")
    public void onCommand(CommandEvent event) {
        var guildId = event.getGuild().getIdLong();

        if (commandContextProvider.pluginJamService().isJamActive(guildId)) {
            event.with().ephemeral(true).reply("error-alreadyActive");
            return;
        }

        var nextJam = commandContextProvider.pluginJamService().getUpComingJam(guildId);
        if (nextJam.isPresent()) {
            commandContextProvider.pluginJamService().startJam(nextJam.get().id());
            event.with().ephemeral(true).reply("command-start-message-activated");
            return;
        }

        event.with().ephemeral(true).reply("error-noupcomingjam");
    }
}
