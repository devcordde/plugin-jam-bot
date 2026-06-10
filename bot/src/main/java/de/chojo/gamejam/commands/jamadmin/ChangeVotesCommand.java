/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.jamadmin;

import com.google.inject.Inject;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
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
        var guilds = commandContextProvider.guilds();
        var jam = guilds.guild(event).jams().activeJam();
        if (jam.isEmpty()) {
            event.reply("error.noactivejam");
            return;
        }
        jam.get().state().voting(voting);
        event.reply("asd");
    }
}
