/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.vote;

import com.google.inject.Inject;
import de.chojo.gamejam.server.CommandContextProvider;
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
        var guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        var voteEntries = jam.user(event.getMember()).votes();
        var given = voteEntries.stream()
                .filter(e -> e.points() != 0)
                .map(e -> e.team().meta().name() + ": **" + e.points() + "**")
                .collect(Collectors.joining("\n"));

        var embed = event.embed("votes-info");
        embed.title("command.votes.info.embed.title");
        embed.description(given);

        event.with().ephemeral(true).embeds(embed).reply();
    }
}
