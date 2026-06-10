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
        var guild = commandContextProvider.guilds().guild(event);
        var jams = guild.jams();
        var currJam = jams.activeJam();
        if (currJam.isPresent()) {
            event.reply("error.alreadyActive");
            return;
        }
        var next = jams.nextOrCurrent();
        if (next.isPresent()) {
            next.get().state().active(true);
            event.reply("command.start.message.activated");
            return;
        }

        event.reply("error.noupcomingjam");
    }
}
