/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.bot.commands.team;

import com.google.inject.Inject;
import de.chojo.pluginjam.bot.commands.CommandContextProvider;
import de.chojo.pluginjam.bot.message.EmbedHelper;
import de.chojo.pluginjam.database.entity.team.Team;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.AutoCompleteEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.embeds.EmbedConfig;
import io.github.kaktushose.jdac.embeds.EmbedDataSource;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Collections;
import java.util.List;

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
        var guildId = event.getGuild().getIdLong();
        var optJam = commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId);

        if (optJam.isEmpty()) {
            event.with().ephemeral(true).reply("error-nojamactive");
            return;
        }
        var jam = optJam.get();
        var teams = commandContextProvider.teamService().getTeamsByJamId(jam.id());

        if (user != null) {
            var userTeam = commandContextProvider.teamService().getUserTeam(user.getIdLong());
            userTeam.ifPresentOrElse(team -> sendProfile(event, team),
                    () -> event.with().ephemeral(true).reply("command-team-profile-message-nouserteam"));
            return;
        }
        if (teamName != null) {
            var namedTeam = commandContextProvider.teamService().getTeamByName(jam.id(), teamName);

            namedTeam.ifPresentOrElse(team -> sendProfile(event, team),
                    () -> event.with().ephemeral(true).reply("error-unkownteam"));
            return;
        }
        var userTeam = commandContextProvider.teamService().getUserTeam(event.getMember().getIdLong());

        userTeam.ifPresentOrElse(team -> sendProfile(event, team),
                () -> event.with().ephemeral(true).reply("error-noteam"));
    }

    private void sendProfile(CommandEvent event, Team team) {
        event.reply(EmbedHelper.teamProfileEmbed(team, event));
    }

    @AutoComplete(value = "team profile", options = "team")
    public void onAutoComplete(AutoCompleteEvent event) {
        var guildId = event.getGuild().getIdLong();
        var teams = commandContextProvider.teamService().getTeamsByJamId(commandContextProvider.pluginJamService().getCurrentOrUpcoming(guildId).get().id());
        var choices = teams.stream().map(team -> team.meta().getTeamName()).map(s -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(s, s)).toList();
        event.replyChoices(choices);
    }
}
