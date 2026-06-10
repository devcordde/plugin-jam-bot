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
import de.chojo.jdautil.wrapper.EventContext;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Bundle("locale")
@Interaction
public class TeamPromoteCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamPromoteCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team promote")
    public void onCommand(CommandEvent event, @Param(value = "user", type = OptionType.USER) User user) {
        JamGuild guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        var member = guild.guild().getMember(user);

        jam.teams().byMember(user).ifPresentOrElse(
                targetTeam -> {
                    if (!targetTeam.isLeader(event.getUser())) {
                        event.with().ephemeral(true).reply("error.noleader");
                        return;
                    }

                    targetTeam.meta().leader(member);
                    event.with().ephemeral(true).reply("command.team.promote.message.done");
                },
                () -> event.with().ephemeral(true).reply("error.noteam"));
    }
}
