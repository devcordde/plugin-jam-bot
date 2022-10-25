/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Leave implements SlashHandler {
    private final Guilds guilds;

    public Leave(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        JamGuild jamGuild = guilds.guild(event);
        var optJam = jamGuild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.message.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.reply(context.localize("error.votingactive")).setEphemeral(true).queue();
            return;
        }

        jam.teams().byMember(event.getMember())
           .ifPresentOrElse(team -> {
               if (!team.isLeader(event.getUser())) {
                   event.reply(context.localize("command.team.leave.message.leaderleave")).setEphemeral(true).queue();
                   return;
               }
               team.member(event.getMember()).ifPresent(member -> {
                   var guild = event.getGuild();
                   member.leave();
                   guild.removeRoleFromMember(event.getMember(), guild.getRoleById(team.meta().role())).queue();
                   guild.getTextChannelById(team.meta().textChannel())
                        .sendMessage(context.localize("command.team.leave.leftBroadcast",
                                Replacement.createMention(event.getMember())))
                        .queue();
                   event.reply(context.localize("command.team.leave.left")).setEphemeral(true).queue();
               });
           }, () -> event.reply(context.localize("error.noteam")).setEphemeral(true).queue());
    }
}
