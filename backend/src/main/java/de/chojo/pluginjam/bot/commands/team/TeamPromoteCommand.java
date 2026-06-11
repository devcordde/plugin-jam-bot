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
import net.dv8tion.jda.api.entities.User;
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
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);

        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }

        var member = event.getGuild().getMember(user);

        var currentTeam = commandContextProvider.teamService().getUserTeam(user.getIdLong());

        currentTeam.ifPresentOrElse((targetTeam) -> {
            if (!targetTeam.isLeader(event.getUser())) {
                event.with().ephemeral(true).reply("error-noleader");
                return;
            }

            targetTeam.meta().getLeaderId(member.getIdLong());
            commandContextProvider.teamService().saveTeam(targetTeam);
            event.with().ephemeral(true).reply("command.team.promote.message.done");
        }, () -> {
            event.with().ephemeral(true).reply("error-noteam");
        });
    }
}
