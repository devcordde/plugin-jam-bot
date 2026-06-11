/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.team;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
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
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);

        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }
        var jam = optJam.get();

        if (jam.state().voting()) {
            event.with().ephemeral(true).reply("error-votingactive");
            return;
        }

        var teamOpt = commandContextProvider.teamService().getUserTeam(event.getMember().getIdLong());
        teamOpt.ifPresentOrElse((team) -> {
            if (team.isLeader(event.getUser())) {
                event.with().ephemeral(true).reply("command-team-leave-message-leaderleave");
                return;
            }

            commandContextProvider.teamService().leaveTeam(team, event.getMember(), event.getGuild());
            event.with().ephemeral(true).reply("command-team-leave-left");
            event.getGuild().getTextChannelById(team.meta().getTextChannelId()).sendMessage("command-team-leave-message-left").queue();
        }, () -> {
            event.with().ephemeral(true).reply("error-noteam");
        });
    }
}
