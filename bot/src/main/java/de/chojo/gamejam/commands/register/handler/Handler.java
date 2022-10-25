/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.register.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.ZonedDateTime;

public class Handler implements SlashHandler {
    private final Guilds guilds;

    public Handler(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.noupcomingjam")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();
        var times = jam.times();

        if (!times.registration().contains(ZonedDateTime.now())) {
            if (times.registration().start().isAfter(ZonedDateTime.now())) {
                event.reply(context.localize("command.register.message.notyet",
                             Replacement.create("TIMESTAMP", TimeFormat.DATE_TIME_LONG.format(times.registration()
                                                                                                   .start()))))
                     .setEphemeral(true)
                     .queue();
                return;
            }
            event.reply(context.localize("command.register.message.notanymore")).setEphemeral(true).queue();
            return;
        }

        if (jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply(context.localize("command.register.message.alreadyregistered")).setEphemeral(true).queue();
            return;
        }

        jam.register(event.getMember());
        var settings = guild.jamSettings();
        var role = event.getGuild().getRoleById(settings.jamRole());
        if (role != null) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
        event.reply(context.localize("command.register.message.registered",
                     Replacement.create("TIMESTAMP", TimeFormat.DATE_TIME_LONG.format(times.jam().start()))))
             .setEphemeral(true)
             .queue();
    }
}
