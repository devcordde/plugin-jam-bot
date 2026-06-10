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
import io.github.kaktushose.jdac.message.placeholder.Entry;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;

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
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);

        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-noupcomingjam");
            return;
        }

        var jam = optJam.get();

        var registrationStart = jam.time().registrationStart();
        var registrationEnd = jam.time().registrationEnd();

        if (registrationStart.isAfter(ChronoLocalDateTime.from(ZonedDateTime.now()))) {
            event.with().ephemeral(true).reply("command-register-message-notyet", Entry.entry("TIMESTAMP", TimeFormat.DATE_TIME_LONG.format(registrationStart)));
            return;
        }

        if (registrationEnd.isBefore(ChronoLocalDateTime.from(ZonedDateTime.now()))) {
            event.with().ephemeral(true).reply("command-register-message-notanymore");
            return;
        }

        if (jam.registrations().stream().anyMatch(member -> member.userId().equals(event.getMember().getIdLong()))) {
            event.with().ephemeral(true).reply("command-register-message-alreadyregistered");
            return;
        }

        commandContextProvider.pluginJamService().registerUser(jam.id(), event.getMember().getIdLong());

        var settings = commandContextProvider.settingsService().getSettings(guildId);
        var role = event.getGuild().getRoleById(settings.getParticipantRole());
        if (role != null) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
        event.with().ephemeral(true).reply(
                "command-register-message-registered",
                Entry.entry("TIMESTAMP", TimeFormat.DATE_TIME_LONG.format(ZonedDateTime.of(jam.time().registrationStart(), ZoneId.of(jam.time().zoneId()))))
        );
    }
}
