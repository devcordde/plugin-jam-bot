/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.commands.team.handler;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.data.dao.JamGuild;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collections;

public final class Profile implements SlashHandler {
    private final Guilds guilds;

    public Profile(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = guilds.guild(event).jams().nextOrCurrent();
        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }
        var jam = optJam.get();
        var teams = jam.teams();

        if (event.getOption("user") != null) {
            teams.byMember(event.getOption("user").getAsMember())
                 .ifPresentOrElse(team -> sendProfile(event, team, context),
                         () -> event.reply(context.localize("command.team.profile.message.nouserteam"))
                                    .setEphemeral(true)
                                    .queue());
            return;
        }
        if (event.getOption("team") != null) {
            teams.byName(event.getOption("team").getAsString())
                 .ifPresentOrElse(team -> sendProfile(event, team, context),
                         () -> event.reply(context.localize("error.unkownteam")).setEphemeral(true).queue());
            return;
        }
        teams.byMember(event.getMember())
             .ifPresentOrElse(team -> sendProfile(event, team, context),
                     () -> event.reply(context.localize("error.noteam")).setEphemeral(true).queue());
    }

    private void sendProfile(SlashCommandInteractionEvent event, Team team, EventContext context) {
        event.replyEmbeds(team.profileEmbed(context.guildLocalizer())).setEphemeral(true).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        JamGuild guild = guilds.guild(event);
        if ("team".equals(event.getFocusedOption().getName())) {
            var jam = guild.jams().nextOrCurrent();
            if (jam.isEmpty()) {
                event.replyChoices(Collections.emptyList()).queue();
                return;
            }
            var teams = jam.get().teams().teams().stream()
                           .filter(team -> team.matchName(event.getFocusedOption().getValue()))
                           .map(team -> team.meta().name())
                           .map(team -> new Command.Choice(team, team))
                           .toList();
            event.replyChoices(teams).queue();
        }
    }
}
