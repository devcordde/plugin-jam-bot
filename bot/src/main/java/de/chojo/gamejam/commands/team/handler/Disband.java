/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.guild.jams.Jam;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public final class Disband implements SlashHandler {
    private final Guilds guilds;

    public Disband(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Optional<Jam> optJam = guilds.guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("error.votingactive")).setEphemeral(true).queue();
            return;
        }

        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply(context.localize("error.noconfirm")).setEphemeral(true).queue();
            return;
        }

        var jamTeam = jam.teams().byMember(event.getMember());
        if (jamTeam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        var team = jamTeam.get();


        var members = team.member();
        for (var teamMember : members) {
            teamMember.member()
                      .getUser()
                      .openPrivateChannel()
                      .flatMap(channel -> channel.sendMessage(context.localize("command.team.disband.message.disbanded")))
                      .queue();
        }

        if (team.disband()) {
            event.reply(context.localize("command.team.disband.message.disbanded")).setEphemeral(true).queue();
        }
    }
}
