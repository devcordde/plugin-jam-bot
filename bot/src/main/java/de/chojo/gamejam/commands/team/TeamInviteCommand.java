/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.server.CommandContextProvider;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Bundle("locale")
@Interaction
public final class TeamInviteCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamInviteCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team invite")
    public void onCommand(CommandEvent event, @Param(value = "user", type = OptionType.USER) User user) {
        JamGuild guild = commandContextProvider.guilds().guild(event);
        var optJam = guild.jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();

        if (jam.state().isVoting()) {
            event.with().ephemeral(true).reply("error.votingactive");
            return;
        }

        var optTeam = jam.teams().byMember(event.getMember());
        if (optTeam.isEmpty()) {
            event.with().ephemeral(true).reply("error.noteam");
            return;
        }

        var team = optTeam.get();

        if (!team.isLeader(event.getUser())) {
            event.with().ephemeral(true).reply("command.team.invite.message.noleader");
            return;
        }

        var members = team.member();
        var settings = guild.jamSettings();

        if (members.size() >= settings.teamSize()) {
            event.with().ephemeral(true).reply("error.maxteamsize");
            return;
        }

        if (!jam.registrations().contains(user.getIdLong())) {
            event.with().ephemeral(true).reply("command.team.invite.message.notRegistered");
            return;
        }

        var currTeam = jam.teams().byMember(user);

        if (currTeam.isPresent()) {
            event.reply("command.team.invite.message.partofteam");
            return;
        }

        var guildId = event.getGuild().getIdLong();
        var teamId = team.id();
        var buttonId = "invite-accept:" + guildId + ":" + teamId + ":" + user.getIdLong();

        var embed = new EmbedBuilder()
                .setTitle("You have been invited to join a team on " + event.getGuild().getName())
                .setDescription(event.getUser().getAsMention() + " has invited you to join team **" + team.meta().name() + "**")
                .build();

        user.openPrivateChannel().queue(channel ->
                channel.sendMessageEmbeds(embed)
                        .setComponents(ActionRow.of(Button.success(buttonId, "Accept")))
                        .queue()
        );

        event.with().ephemeral(true).reply("command.team.invite.message.send");
    }
}
