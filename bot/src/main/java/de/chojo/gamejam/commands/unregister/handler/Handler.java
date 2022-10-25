/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.unregister.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
            event.reply(context.localize("error.noupcomingjam"))
                 .setEphemeral(true)
                 .queue();
            return;
        }

        var jam = optJam.get();

        if (!jam.registrations().contains(event.getMember().getIdLong())) {
            event.reply(context.localize("command.unregister.message.notregistered")).setEphemeral(true).queue();
            return;
        }

        jam.teams().byMember(event.getMember())
                .ifPresentOrElse(
                        team -> event.reply(context.localize("command.unregister.message.inteam")).queue(),
                        () -> {
                            var settings = guild.jamSettings();
                            var role = event.getGuild().getRoleById(settings.jamRole());
                            if (role != null) {
                                event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                            }
                            event.reply(context.localize("command.unregister.message.unregistered"))
                                 .setEphemeral(true)
                                 .queue();

                        });
    }
}
