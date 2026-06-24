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
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;

import java.util.Optional;

@Bundle("locale")
@Interaction
public final class TeamDisbandCommand {
    private final CommandContextProvider commandContextProvider;
    private final MessageResolver messageResolver;

    @Inject
    public TeamDisbandCommand(CommandContextProvider commandContextProvider, MessageResolver messageResolver) {
        this.commandContextProvider = commandContextProvider;
        this.messageResolver = messageResolver;
    }

    @Command(value = "team disband")
    public void onCommand(CommandEvent event, @Param("confirm") boolean confirm) {
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(event.getGuild().getIdLong());

        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }
        var jam = optJam.get();

        if (jam.state().voting()) {
            event.with().ephemeral(true).reply("error-votingactive");
            return;
        }

        if (!confirm) {
            event.with().ephemeral(true).reply("error-noconfirm");
            return;
        }

        var existingTeamOpt = commandContextProvider.teamService().getUserTeam(event.getMember().getIdLong());
        if (existingTeamOpt.isEmpty()) {
            event.with().ephemeral(true).reply("error-noteam");
            return;
        }

        var team = existingTeamOpt.get();

        var members = team.members();
        for (var teamMember : members) {
            var member = event.getGuild().getMemberById(teamMember.userId());
            var user = member.getUser();
            user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(messageResolver.resolve("command-team-disband-message-disbanded", member.getGuild().getLocale())))
                    .queue();
        }

        commandContextProvider.teamService().disbandTeam(team, event.getGuild());
        event.with().ephemeral(true).reply("command-team-disband-message-disbanded");
    }
}
