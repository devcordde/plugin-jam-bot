/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.register;

import com.google.inject.Inject;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
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
        var guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply("error.noupcomingjam");
            return;
        }

        var jam = optJam.get();

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply("command.unregister.message.notregistered");
            return;
        }

        jam.teams().byMember(event.getMember()).ifPresentOrElse(
                team -> event.reply("command.unregister.message.inteam"),
                () -> {
                    var settings = guild.jamSettings();
                    var role = event.getGuild().getRoleById(settings.jamRole());
                    if (role != null) {
                        event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                    }
                    event.reply("command.unregister.message.unregistered");

                });
    }
}
