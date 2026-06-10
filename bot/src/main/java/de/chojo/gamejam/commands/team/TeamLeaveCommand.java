/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public final class TeamLeaveCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamLeaveCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team leave")
    public void onCommand(CommandEvent event) {
        var jamGuild = commandContextProvider.guilds().guild(event);
        var optJam = jamGuild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.with().ephemeral(true).reply("error.votingactive");
            return;
        }

        jam.teams().byMember(event.getMember()).ifPresentOrElse(team -> {
            if (!team.isLeader(event.getUser())) {
                event.with().ephemeral(true).reply("command.team.leave.message.leaderleave");
                return;
            }
            team.member(event.getMember()).ifPresent(member -> {
                member.leave();
                team.meta().textChannel().ifPresent(channel -> {
                    //TODO: reimplement message
                });
                event.with().ephemeral(true).reply("command.team.leave.left");
            });
        }, () -> event.with().ephemeral(true).reply("error.noteam"));
    }
}
