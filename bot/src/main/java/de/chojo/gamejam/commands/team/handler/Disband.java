/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.TeamData;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class Disband implements SlashHandler {
    private final TeamData teamData;
    private final JamData jamData;

    public Disband(TeamData teamData, JamData jamData) {
        this.teamData = teamData;
        this.jamData = jamData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = jamData.getNextOrCurrentJam(event.getGuild());
        if (optJam.isEmpty()) {
            event.reply(context.localize("command.team.message.nojamactive")).setEphemeral(true).queue();
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

        var jamTeam = teamData.getTeamByMember(jam, event.getMember());
        if (jamTeam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        var team = jamTeam.get();


        var members = teamData.getMember(team);
        for (var teamMember : members) {
            event.getJDA().getShardManager().retrieveUserById(teamMember.userId())
                 .flatMap(u -> event.getGuild().retrieveMember(u))
                 .queue(member -> {
                     member.getUser().openPrivateChannel()
                           .flatMap(channel -> channel.sendMessage(context.localize("command.team.disband.message.disbanded")))
                           .queue();
                 });
        }

        team.delete(event.getGuild());
        teamData.disbandTeam(team);
        event.reply(context.localize("command.team.disband.message.disbanded")).setEphemeral(true).queue();
    }
}
