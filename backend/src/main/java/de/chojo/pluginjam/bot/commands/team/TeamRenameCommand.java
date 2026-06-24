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

@Bundle("locale")
@Interaction
public class TeamRenameCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamRenameCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team rename")
    public void onCommand(CommandEvent event, @Param("name") String teamName) {
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }
        var jam = optJam.get();

        var existingTeam = commandContextProvider.teamService().getTeamByName(jam.id(), teamName);
        if (existingTeam.isPresent()) {
            event.with().ephemeral(true).reply("command-team-create-message-nametaken");
            return;
        }

        var optCurrTeam = commandContextProvider.teamService().getUserTeam(event.getMember().getIdLong());

        if (optCurrTeam.isEmpty()) {
            event.with().ephemeral(true).reply("error-noteam");
            return;
        }

        var team = optCurrTeam.get();

        if (!team.isLeader(event.getUser())) {
            event.with().ephemeral(true).reply("error-noleader");
            return;
        }

        team.meta().getTeamName(teamName);

        commandContextProvider.teamService().saveTeam(team);
        event.with().ephemeral(true).reply("command-team-rename-message-done");
    }
}
