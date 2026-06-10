/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team;

import com.google.inject.Inject;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.server.CommandContextProvider;
import de.chojo.jdautil.wrapper.EventContext;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.AutoComplete;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Collections;

@Bundle("locale")
@Interaction
public final class TeamProfileCommand {
    private final CommandContextProvider commandContextProvider;

    @Inject
    public TeamProfileCommand(CommandContextProvider commandContextProvider) {
        this.commandContextProvider = commandContextProvider;
    }

    @Command(value = "team profile")
    public void onCommand(
            CommandEvent event,
            @Param(name = "user", optional = true, type = OptionType.USER) User user,
            @Param(value = "team", optional = true) String teamName
    ) {
        var optJam = commandContextProvider.guilds().guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error.nojamactive");
            return;
        }
        var jam = optJam.get();
        var teams = jam.teams();

        if (user != null) {
            teams.byMember(user).ifPresentOrElse(team ->
                            sendProfile(event, team),
                    () -> event.with().ephemeral(true).reply("command.team.profile.message.nouserteam"));
            return;
        }
        if (teamName != null) {
            teams.byName(teamName).ifPresentOrElse(team ->
                            sendProfile(event, team),
                    () -> event.with().ephemeral(true).reply("error.unkownteam"));
            return;
        }

        teams.byMember(event.getMember()).ifPresentOrElse(team ->
                        sendProfile(event, team),
                () -> event.with().ephemeral(true).reply("error.noteam"));
    }

    private void sendProfile(CommandEvent event, Team team) {
        event.with().ephemeral(true).embeds(team.profileEmbed(event)).reply();
    }

    @AutoComplete(value = "team profile", options = "team")
    public void onAutoComplete(AutoCompleteEvent event) {
        var guild = commandContextProvider.guilds().guild(event);
        var choices = guild.jams().nextOrCurrent()
                .map(jam -> jam.teams().completeTeam(event.getValue()))
                .orElse(Collections.emptyList());
        event.replyChoices(choices);
    }
}
