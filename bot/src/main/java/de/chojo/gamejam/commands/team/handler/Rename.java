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

public class Rename implements SlashHandler {
    private final Guilds guilds;

    public Rename(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        JamGuild guild = guilds.guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        jam.teams().byName(event.getOption("name").getAsString())
           .ifPresentOrElse(
                   team -> event.reply(context.localize("command.team.create.message.nametaken")).setEphemeral(true)
                                .queue(),
                   () -> {
                       var optCurrTeam = jam.teams().byMember(event.getUser());

                       if (optCurrTeam.isEmpty()) {
                           event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
                           return;
                       }

                       var team = optCurrTeam.get();

                       if (!team.isLeader(event.getUser())) {
                           event.reply(context.localize("error.noleader")).setEphemeral(true).queue();
                           return;
                       }

                       team.meta().rename(event.getOption("name").getAsString());
                       event.reply(context.localize("command.team.rename.message.done")).setEphemeral(true).queue();
                   });
    }
}
