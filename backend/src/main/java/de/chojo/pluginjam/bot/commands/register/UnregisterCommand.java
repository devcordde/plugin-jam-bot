/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.register;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.CommandScope;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Bundle("locale")
@Interaction
public class UnregisterCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public UnregisterCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "unregister")
    public void onCommand(CommandEvent event) {
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-noupcomingjam");
            return;
        }

        var jam = optJam.get();

        if (jam.registrations().stream().noneMatch(r -> r.userId().equals(event.getMember().getIdLong()))) {
            event.with().ephemeral(true).reply("command-unregister-message-notregistered");
            return;
        }

        var optTeam = commandContextProvider.teamService().getUserTeam(event.getMember().getIdLong());
        if (optTeam.isPresent()) {
            event.with().ephemeral(true).reply("command-unregister-message-inteam");
            return;
        }

        commandContextProvider.pluginJamService().unregisterUser(jam.id(), event.getMember().getIdLong());

        var settings = commandContextProvider.settingsService().getSettings(guildId);
        var role = event.getGuild().getRoleById(settings.getParticipantRole());
        if (role != null) {
            event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
        }
        event.with().ephemeral(true).reply("command-unregister-message-unregistered");
    }
}
