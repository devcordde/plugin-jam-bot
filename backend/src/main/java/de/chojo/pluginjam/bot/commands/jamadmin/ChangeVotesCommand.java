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
public final class ChangeVotesCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public ChangeVotesCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command("jamdamin changevotes")
    public void onCommand(CommandEvent event, @Param("voting") boolean voting) {
        var guildId = event.getGuild().getIdLong();
        var activeJam = commandContextProvider.pluginJamService().getActiveJam(guildId);

        if (activeJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-noactivejam");
            return;
        }

        commandContextProvider.pluginJamService().setVoting(activeJam.get().id(), voting);
        event.with().ephemeral(true).reply("command-jamadmin-changevotes-message-changed");
    }
}
