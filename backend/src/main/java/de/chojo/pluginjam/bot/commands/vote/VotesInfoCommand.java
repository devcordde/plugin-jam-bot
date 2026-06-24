/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.vote;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

import java.util.stream.Collectors;

@Bundle("locale")
@Interaction
public class VotesInfoCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public VotesInfoCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "votes info")
    public void onCommand(CommandEvent event) {
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getActiveJam(guildId);
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }
        var jam = optJam.get();

        // TODO: implement a nice info message
    }
}
