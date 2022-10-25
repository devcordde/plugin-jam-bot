/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Promote implements SlashHandler {
    private final Guilds guilds;

    public Promote(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        JamGuild guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.message.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        var user = event.getOption("user").getAsMember();
        jam.teams().byMember(user)
           .ifPresentOrElse(
                   team -> {
                       if (!team.isLeader(event.getUser())) {
                           event.reply(context.localize("error.noleader")).setEphemeral(true).queue();
                           return;
                       }

                       if (user.getRoles().stream().noneMatch(role -> role.getIdLong() == team.meta().role())) {
                           event.reply(context.localize("command.team.promote.message.notinteam")).queue();
                           return;
                       }

                       team.meta().leader(user.getIdLong());
                       event.reply(context.localize("command.team.promote.message.done")).setEphemeral(true).queue();

                   },
                   () -> event.reply(context.localize("error.noteam")).setEphemeral(true).queue());
    }
}
