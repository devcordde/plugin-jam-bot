/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.jamadmin;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public final class JamEndCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public JamEndCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "jamadmin end")
    public void onCommand(CommandEvent event, @Param("confirm") boolean confirm) {
        if (!confirm) {
            event.with().ephemeral(true).reply("error-noconfirm");
            return;
        }

        var guildId = event.getGuild().getIdLong();

        if (!commandContextProvider.pluginJamService().isJamActive(guildId)) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }

        commandContextProvider.pluginJamService().endActiveJams(guildId);
        event.with().ephemeral(true).reply("command-jamadmin-jam-end-message-ended");
    }
}
