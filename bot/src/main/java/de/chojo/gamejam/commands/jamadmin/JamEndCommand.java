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
public final class JamEndCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public JamEndCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "jamadmin end")
    public void onCommand(CommandEvent event, @Param("confirm") boolean confirm) {
        if (!confirm) {
            event.reply("error.noconfirm");
            return;
        }

        commandContextProvider.guilds().guild(event).jams().activeJam().ifPresentOrElse(
                jam -> {
                    jam.state().finish();
                    event.reply("command.jamadmin.jam.end.message.ended");
                },
                () -> event.reply("error.noactivejam")
        );
    }
}
