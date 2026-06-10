/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
        JamGuild guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        jam.teams().byName(teamName).ifPresentOrElse(
                team -> event.with().ephemeral(true).reply("command.team.create.message.nametaken"),
                () -> {
                    var optCurrTeam = jam.teams().byMember(event.getUser());

                    if (optCurrTeam.isEmpty()) {
                        event.with().ephemeral(true).reply("error.noteam");
                        return;
                    }

                    var team = optCurrTeam.get();

                    if (!team.isLeader(event.getUser())) {
                        event.with().ephemeral(true).reply("error.noleader");
                        return;
                    }

                    team.meta().rename(teamName);
                    event.with().ephemeral(true).reply("command.team.rename.message.done");
                });
    }
}
