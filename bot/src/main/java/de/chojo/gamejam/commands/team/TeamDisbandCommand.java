/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

import java.util.Optional;

@Bundle("locale")
@Interaction
public final class TeamDisbandCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamDisbandCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team disband")
    public void onCommand(CommandEvent event, @Param("confirm") boolean confirm) {
        Optional<Jam> optJam = commandContextProvider.guilds().guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.with().ephemeral(true).reply("error.votingactive");
            return;
        }

        if (!confirm) {
            event.with().ephemeral(true).reply("error.noconfirm");
            return;
        }

        var jamTeam = jam.teams().byMember(event.getMember());
        if (jamTeam.isEmpty()) {
            event.with().ephemeral(true).reply("error.noteam");
            return;
        }

        var team = jamTeam.get();


        var members = team.member();
        for (var teamMember : members) {
            teamMember.member()
                    .getUser()
                    .openPrivateChannel()
                    //TODO: fix localization
                    .flatMap(channel -> channel.sendMessage("command.team.disband.message.disbanded"))
                    .queue();
        }

        if (team.disband()) {
            event.with().ephemeral(true).reply("command.team.disband.message.disbanded");
        }
    }
}
