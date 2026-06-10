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
import io.github.kaktushose.jdac.message.placeholder.Entry;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.ZonedDateTime;

@Bundle("locale")
@Interaction
public class RegisterCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public RegisterCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "register")
    public void onCommand(CommandEvent event) {
        var guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply("error.noupcomingjam");
            return;
        }
        var jam = optJam.get();
        var times = jam.times();

        if (!times.registration().contains(ZonedDateTime.now())) {
            if (times.registration().start().isAfter(ZonedDateTime.now())) {
                event.reply(
                        "command.register.message.notyet",
                        Entry.entry("TIMESTAMP", TimeFormat.DATE_TIME_LONG.format(times.registration().start()))
                );
                return;
            }
            event.reply("command.register.message.notanymore");
            return;
        }

        if (jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply("command.register.message.alreadyregistered");
            return;
        }

        jam.register(event.getMember());
        var settings = guild.jamSettings();
        var role = event.getGuild().getRoleById(settings.jamRole());
        if (role != null) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
        event.reply(
                "command.register.message.registered",
                Entry.entry("TIMESTAMP", TimeFormat.DATE_TIME_LONG.format(times.registration().start()))
        );
    }
}
